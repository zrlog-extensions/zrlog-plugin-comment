import React from "react";
import {
    Button,
    ColorPicker,
    Divider,
    Form,
    Input,
    message,
    Select,
    Switch,
    Table,
    Modal,
    Tag,
    Card,
    theme
} from "antd";
import {
    SettingOutlined,
    ReloadOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    InfoCircleOutlined
} from "@ant-design/icons";
import {BaseSetting, ChangyanSetting, PluginCoreInfoResponse} from "../index";
import axios from "axios";
import styled from "styled-components";

type CoreIndexProps = {
    data: PluginCoreInfoResponse;
}

const Shell = styled.div<{ $token: any }>`
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: ${props => props.$token.colorText};
`;

const HeaderPanel = styled.div<{ $token: any }>`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: ${props => props.$token.colorBgContainer};
  border-radius: ${props => props.$token.borderRadiusLG}px;
  box-shadow: ${props => props.$token.boxShadowTertiary || "none"};
  border: 1px solid ${props => props.$token.colorBorderSecondary};
  transition: all 0.3s ease;

  @media (max-width: 600px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
`;

const HeaderLeft = styled.div`
  display: flex;
  flex-direction: column;
  gap: 6px;
`;

const MainTitle = styled.h1<{ $token: any }>`
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: ${props => props.$token.colorTextHeading};
  display: flex;
  align-items: center;
  gap: 8px;
`;

const Subtitle = styled.div<{ $token: any }>`
  font-size: 13px;
  color: ${props => props.$token.colorTextDescription};
`;

const Actions = styled.div`
  display: flex;
  gap: 12px;
`;

const LogCard = styled(Card)<{ $token: any }>`
  background: ${props => props.$token.colorBgContainer} !important;
  border-radius: ${props => props.$token.borderRadiusLG}px !important;
  box-shadow: ${props => props.$token.boxShadowTertiary || "none"} !important;
  border: 1px solid ${props => props.$token.colorBorderSecondary} !important;
  overflow: hidden;
  
  .ant-card-body {
    padding: 20px !important;
  }
`;

const FormScrollArea = styled.div`
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
  overflow-x: hidden;
`;

const InfoBox = styled.div<{ $token: any }>`
  background: ${props => props.$token.colorFillTertiary};
  padding: 12px 16px;
  border-radius: ${props => props.$token.borderRadius}px;
  border-left: 4px solid ${props => props.$token.colorPrimary};
  margin-bottom: 20px;
  font-size: 13px;
  color: ${props => props.$token.colorTextSecondary};
  display: flex;
  align-items: flex-start;
  gap: 8px;
`;

const CoreIndex: React.FC<CoreIndexProps> = ({data}) => {
    const {token} = theme.useToken();
    const colorPrimary = data.primaryColor || data.colorPrimary || token.colorPrimary;

    const [changyan, setChangyan] = React.useState<ChangyanSetting>(() => {
        try {
            return JSON.parse(data.setting.changyan);
        } catch (e) {
            return { appId: "", appKey: "", callbackUrl: "" };
        }
    });
    
    const [base, setBase] = React.useState<BaseSetting>(() => {
        try {
            return JSON.parse(data.setting.base);
        } catch (e) {
            return { styleStr: "", baseUrl: "", mainColor: "" };
        }
    });

    const [type, setType] = React.useState<string>(data.setting.type || "base");
    const [commentEmailNotify, setCommentEmailNotify] = React.useState<boolean>(data.setting.commentEmailNotify || false);
    const [history, setHistory] = React.useState<any[]>(() => {
        try {
            return data.setting.syncHistory ? (typeof data.setting.syncHistory === "string" ? JSON.parse(data.setting.syncHistory) : data.setting.syncHistory) : [];
        } catch (e) {
            return [];
        }
    });

    const [messageApi, contextHolder] = message.useMessage({maxCount: 3});
    const [settingsVisible, setSettingsVisible] = React.useState<boolean>(false);
    const [saveLoading, setSaveLoading] = React.useState<boolean>(false);
    const [tableLoading, setTableLoading] = React.useState<boolean>(false);
    const [formType, setFormType] = React.useState<string>(type);

    const [form] = Form.useForm();

    const reloadData = async () => {
        setTableLoading(true);
        try {
            const { data: res } = await axios.get("json");
            if (res.setting) {
                setChangyan(JSON.parse(res.setting.changyan));
                setBase(JSON.parse(res.setting.base));
                setType(res.setting.type || "base");
                setCommentEmailNotify(res.setting.commentEmailNotify || false);
                
                let historyList = [];
                if (res.setting.syncHistory) {
                    historyList = typeof res.setting.syncHistory === "string" ? JSON.parse(res.setting.syncHistory) : res.setting.syncHistory;
                }
                setHistory(historyList);
            }
            messageApi.success("数据已刷新");
        } catch (e) {
            messageApi.error("刷新数据失败");
        } finally {
            setTableLoading(false);
        }
    };

    const handleSave = async (values: any) => {
        setSaveLoading(true);
        try {
            const updatedChangyan = {
                appId: values.appId || "",
                appKey: values.appKey || "",
                callbackUrl: values.callbackUrl || changyan.callbackUrl,
            };
            const updatedBase = {
                styleStr: values.styleStr || "",
                mainColor: typeof values.mainColor === "string" ? values.mainColor : (values.mainColor?.toHexString?.() || values.mainColor || ""),
                baseUrl: values.baseUrl || "",
            };

            const params = new URLSearchParams();
            params.set("type", values.type);
            params.set("commentEmailNotify", (values.commentEmailNotify ? "true" : "false"));
            params.set("changyan", JSON.stringify(updatedChangyan));
            params.set("base", JSON.stringify(updatedBase));

            const { data: res } = await axios.post("update", params.toString());
            if (res.success) {
                messageApi.success("保存配置成功");
                setSettingsVisible(false);
                setTimeout(reloadData, 500);
            } else {
                messageApi.error("保存失败");
            }
        } catch (e: any) {
            messageApi.error("保存请求失败: " + e.message);
        } finally {
            setSaveLoading(false);
        }
    };

    const openSettings = () => {
        setFormType(type);
        form.setFieldsValue({
            type,
            commentEmailNotify,
            appId: changyan.appId,
            appKey: changyan.appKey,
            callbackUrl: changyan.callbackUrl,
            styleStr: base.styleStr,
            mainColor: base.mainColor || colorPrimary,
            baseUrl: base.baseUrl
        });
        setSettingsVisible(true);
    };

    const columns = [
        {
            title: "日志记录时间",
            dataIndex: "time",
            key: "time",
            width: 180,
            render: (text: string) => <span style={{ fontFamily: "monospace" }}>{text}</span>
        },
        {
            title: "操作日志/消息",
            dataIndex: "message",
            key: "message",
            render: (text: string) => <span>{text}</span>
        },
        {
            title: "同步/变更数量",
            dataIndex: "count",
            key: "count",
            width: 130,
            align: "center" as const,
            render: (count: number) => count > 0 ? <Tag color="blue">{count} 条</Tag> : <span style={{ color: token.colorTextTertiary }}>-</span>,
        },
        {
            title: "执行状态",
            dataIndex: "success",
            key: "success",
            width: 110,
            align: "center" as const,
            render: (success: boolean) => (
                <Tag color={success ? "success" : "error"} icon={success ? <CheckCircleOutlined /> : <CloseCircleOutlined />}>
                    {success ? "成功" : "失败"}
                </Tag>
            ),
        },
    ];

    return (
        <Shell $token={token}>
            {contextHolder}
            
            <HeaderPanel $token={token}>
                <HeaderLeft>
                    <MainTitle $token={token}>
                        <div style={{ width: 6, height: 24, borderRadius: 3, background: colorPrimary }} />
                        {data.plugin.name} 管理中心
                    </MainTitle>
                    <Subtitle $token={token}>
                        版本号: v{data.plugin.version} | 当前模式: <Tag color={type === "base" ? "processing" : "warning"}>{type === "base" ? "默认评论框" : "畅言评论框"}</Tag>
                    </Subtitle>
                </HeaderLeft>
                
                <Actions>
                    <Button 
                        type="default" 
                        icon={<ReloadOutlined />} 
                        loading={tableLoading} 
                        onClick={reloadData}
                    >
                        刷新日志
                    </Button>
                    <Button 
                        type="primary" 
                        icon={<SettingOutlined />} 
                        onClick={openSettings}
                    >
                        配置参数
                    </Button>
                </Actions>
            </HeaderPanel>

            <LogCard $token={token} title="操作与反向同步日志">
                <Table 
                    columns={columns} 
                    dataSource={history.map((item, idx) => ({ ...item, key: idx })).reverse()} 
                    loading={tableLoading}
                    pagination={{ pageSize: 10, size: "small" }}
                    locale={{ emptyText: "暂无操作与同步历史日志" }}
                    size="middle"
                />
            </LogCard>

            <Modal
                title={`${data.plugin.name} 配置面板`}
                open={settingsVisible}
                onOk={() => form.submit()}
                onCancel={() => setSettingsVisible(false)}
                okButtonProps={{ loading: saveLoading }}
                destroyOnClose
                width={620}
            >
                <Form
                    form={form}
                    layout="vertical"
                    onFinish={handleSave}
                    style={{ marginTop: 16 }}
                >
                    <FormScrollArea>
                        <InfoBox $token={token}>
                            <InfoCircleOutlined style={{ marginTop: 2, color: colorPrimary }} />
                            <div>
                                配置项保存后将自动记录到数据库持久化日志中。如果使用畅言模式，请确保填写正确的 appId 和 appKey 以保障数据抓取反向同步。
                            </div>
                        </InfoBox>

                        <Form.Item label="评论框类型" name="type" rules={[{ required: true }]}>
                            <Select onChange={setFormType}>
                                <Select.Option value="base">默认评论框</Select.Option>
                                <Select.Option value="changyan">畅言评论框</Select.Option>
                            </Select>
                        </Form.Item>

                        <Form.Item label="开启新评论邮件通知" name="commentEmailNotify" valuePropName="checked">
                            <Switch />
                        </Form.Item>

                        <Divider />

                        {formType === "base" ? (
                            <React.Fragment>
                                <Form.Item label="自定义评论框 CSS 样式" name="styleStr">
                                    <Input.TextArea placeholder=".comment-item { border-bottom: 1px solid #eee; }" rows={5} />
                                </Form.Item>
                                
                                <Form.Item label="主色彩" name="mainColor">
                                    <ColorPicker showText />
                                </Form.Item>

                                <Form.Item label="评论根 URL (BaseURL)" name="baseUrl" tooltip="默认为空表示采用本插件自带API">
                                    <Input placeholder="https://example.com" />
                                </Form.Item>
                            </React.Fragment>
                        ) : (
                            <React.Fragment>
                                <Form.Item label="App ID (畅言)" name="appId" rules={[{ required: true, message: "请输入畅言 App ID" }]}>
                                    <Input placeholder="请输入畅言 appId" />
                                </Form.Item>

                                <Form.Item label="App Key (畅言)" name="appKey" rules={[{ required: true, message: "请输入畅言 App Key" }]}>
                                    <Input.Password placeholder="请输入畅言 appKey" />
                                </Form.Item>

                                <Form.Item label="回调同步接口 URL" name="callbackUrl" tooltip="后端自动计算，不可修改">
                                    <Input disabled />
                                </Form.Item>
                            </React.Fragment>
                        )}
                    </FormScrollArea>
                </Form>
            </Modal>
        </Shell>
    );
};

export default CoreIndex;
