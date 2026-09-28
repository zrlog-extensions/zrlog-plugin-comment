import React from "react";
import {Alert, Button, Card, Input, message, Modal, Popconfirm, Space, Table, Tag, Typography} from "antd";
import {ReloadOutlined, SafetyCertificateOutlined} from "@ant-design/icons";
import axios from "axios";

interface Suggestion { verdict: "normal" | "spam" | "review"; reason: string; reply: string }
interface Review {
    id: string;
    status: "pending" | "publishing";
    createdAt: number;
    comment: { name: string; content: string; logId: number };
    suggestion?: Suggestion;
}

const verdicts = {normal: "倾向正常", spam: "疑似垃圾", review: "需要人工判断"};

const ModerationPanel = ({adminToken, enabled, aiEnabled, type}: {
    adminToken: string; enabled: boolean; aiEnabled: boolean; type: string;
}) => {
    const [records, setRecords] = React.useState<Review[]>([]);
    const [loading, setLoading] = React.useState(false);
    const [busy, setBusy] = React.useState("");
    const [selected, setSelected] = React.useState<Review | null>(null);
    const [draft, setDraft] = React.useState("");
    const [messageApi, contextHolder] = message.useMessage();

    const reload = React.useCallback(async () => {
        setLoading(true);
        try {
            const {data} = await axios.get("moderationList");
            if (!data.success) throw new Error(data.message);
            setRecords((data.data || []).slice().reverse());
        } catch (error) {
            messageApi.error(error instanceof Error ? error.message : "读取待审评论失败");
        } finally { setLoading(false); }
    }, [messageApi]);

    React.useEffect(() => { void reload(); }, [reload]);

    const act = async (action: string, record: Review) => {
        setBusy(record.id + action);
        try {
            const params = new URLSearchParams({id: record.id, adminToken});
            const {data} = await axios.post(action, params.toString());
            if (!data.success) throw new Error(data.message || "操作失败");
            if (action === "moderationAnalyze") {
                setSelected({...record, suggestion: data.data});
                setDraft(data.data.reply || "");
                messageApi.success("AI 建议已生成，请结合评论内容判断");
            } else {
                setSelected(null);
                messageApi.success(action === "moderationApprove" ? "评论已发布" : "待审记录已移除");
            }
        } catch (error) {
            messageApi.error(error instanceof Error ? error.message : "操作失败，请稍后重试");
        } finally {
            await reload();
            setBusy("");
        }
    };

    return <Card title={<Space><SafetyCertificateOutlined />待审评论 <Tag>{records.length}</Tag></Space>}
                 extra={<Button icon={<ReloadOutlined />} onClick={reload} loading={loading}>刷新</Button>}>
        {contextHolder}
        <Alert type={enabled && type === "base" ? "info" : "warning"} showIcon style={{marginBottom: 16}}
               title={type === "base"
                   ? (enabled ? "新评论审核通过后才会公开。AI 仅提供参考建议。" : "先审后发尚未开启，可在配置参数中开启；已有待审记录仍可处理。")
                   : "当前使用畅言，评论审核由畅言处理；下方保留此前的内置评论待审记录。"} />
        <Table<Review> rowKey="id" dataSource={records} loading={loading} pagination={{pageSize: 8, size: "small"}}
            scroll={{x: 700}} locale={{emptyText: "暂无待审评论"}} columns={[
                {title: "评论", key: "comment", render: (_, record) => <div style={{whiteSpace: "normal", minWidth: 200, maxWidth: 400}}>
                    <Typography.Text strong>{record.comment.name}</Typography.Text>
                    <Typography.Text type="secondary"> · 文章 #{record.comment.logId}</Typography.Text>
                    <div style={{whiteSpace: "pre-wrap", overflowWrap: "anywhere", margin: "8px 0"}}>{record.comment.content.slice(0, 180)}{record.comment.content.length > 180 ? "…" : ""}</div>
                    <Typography.Text type="secondary">{new Date(record.createdAt).toLocaleString()}</Typography.Text>
                </div>},
                {title: "审核建议", key: "suggestion", width: 145, render: (_, record) => record.status === "publishing"
                    ? <Tag color="warning">发布结果待核对</Tag>
                    : record.suggestion ? <Tag color={record.suggestion.verdict === "spam" ? "error" : record.suggestion.verdict === "normal" ? "success" : "warning"}>{verdicts[record.suggestion.verdict]}</Tag>
                    : <Typography.Text type="secondary">尚未分析</Typography.Text>},
                {title: "操作", key: "actions", width: 220, render: (_, record) => <Space wrap>
                    <Button size="small" onClick={() => {setSelected(record); setDraft(record.suggestion?.reply || "");}}>详情</Button>
                    <Button size="small" disabled={!aiEnabled || Boolean(busy) || record.status !== "pending"}
                            loading={busy === record.id + "moderationAnalyze"} onClick={() => act("moderationAnalyze", record)}>AI 分析</Button>
                    {record.status === "pending" && <Popconfirm title="确认公开这条评论？" onConfirm={() => act("moderationApprove", record)}>
                        <Button size="small" type="primary" disabled={Boolean(busy)}>通过</Button>
                    </Popconfirm>}
                    <Popconfirm title={record.status === "pending" ? "删除这条待审评论？" : "已在站点评论管理核对发布结果？"}
                                description={record.status === "publishing" ? "只移除待审记录，不会删除已发布的评论。" : "此操作无法撤销。"}
                                onConfirm={() => act("moderationRemove", record)}>
                        <Button size="small" danger disabled={Boolean(busy)}>{record.status === "pending" ? "删除" : "核对完成"}</Button>
                    </Popconfirm>
                </Space>}
            ]} />
        <Modal title="评论审核详情" open={Boolean(selected)} onCancel={() => setSelected(null)} footer={null} width={680}>
            {selected && <Space orientation="vertical" style={{width: "100%"}} size="middle">
                <Typography.Text strong>{selected.comment.name} · 文章 #{selected.comment.logId}</Typography.Text>
                <div style={{whiteSpace: "pre-wrap", overflowWrap: "anywhere", maxHeight: 240, overflowY: "auto"}}>{selected.comment.content}</div>
                {selected.status === "publishing" && <Alert type="warning" showIcon title="发布请求已发送，结果尚未确认。请先在站点评论管理中核对，避免重复发布。" />}
                {selected.suggestion && <>
                    <Alert type="info" title={verdicts[selected.suggestion.verdict]} description={selected.suggestion.reason} showIcon />
                    {selected.suggestion.reply && <>
                        <Typography.Text strong>回复草稿</Typography.Text>
                        <Input.TextArea value={draft} onChange={event => setDraft(event.target.value)} autoSize={{minRows: 3, maxRows: 8}} />
                        <Typography.Paragraph copyable={{text: draft}}>复制修改后的草稿</Typography.Paragraph>
                        <Typography.Text type="secondary">请核实内容后使用，草稿不会自动发送。</Typography.Text>
                    </>}
                </>}
            </Space>}
        </Modal>
    </Card>;
};

export default ModerationPanel;
