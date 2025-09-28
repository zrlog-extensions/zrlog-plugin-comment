import React from "react";
import {Button, Divider, Form, Input, message, Select, Switch} from "antd";
import Title from "antd/es/typography/Title";
import {Content} from "antd/es/layout/layout";
import {BaseSetting, ChangyanSetting, PluginCoreInfoResponse} from "../index";
import {Option} from "rc-select";
import axios from "axios";
import FormItem from "antd/es/form/FormItem";
import TextArea from "antd/es/input/TextArea";


type CoreIndexProps = {
    data: PluginCoreInfoResponse;
}

const CoreIndex: React.FC<CoreIndexProps> = ({data}) => {

    const [changyan, setChangyan] = React.useState<ChangyanSetting>(JSON.parse(data.setting.changyan));
    const [base, setBase] = React.useState<BaseSetting>(JSON.parse(data.setting.base));
    const [type, setType] = React.useState<string>(data.setting.type);
    const [commentEmailNotify, setCommentEmailNotify] = React.useState<boolean>(data.setting.commentEmailNotify);
    const [messageApi, contextHolder] = message.useMessage({maxCount: 3});
    const [loading, setLoading] = React.useState<boolean>(false);

    const getConfig = () => {
        if (type == "base") {
            return <Form key={"base"}>
                <FormItem label={"评论框样式"}>
                    <TextArea placeholder={"css,"} rows={6} defaultValue={base.styleStr}
                              onChange={(e) => setBase((prevState) => {
                                  return {
                                      ...prevState,
                                      styleStr: e.target.value,
                                  }
                              })}/>

                </FormItem>
                <FormItem label={"评论 BaseURL"}>
                    <Input placeholder={"https://example.com/"} defaultValue={base.baseUrl}
                           onChange={(e) => setBase((prevState) => {
                               return {
                                   ...prevState,
                                   baseUrl: e.target.value,
                               }
                           })}/>
                </FormItem>
            </Form>
        }
        return <Form key={"changyan"}>
            <FormItem label={"appId"}>
                <Input placeholder={"appId"} defaultValue={changyan.appId} onChange={(e) => setChangyan((prevState) => {
                    return {
                        ...prevState,
                        appId: e.target.value,
                    }
                })}/>
            </FormItem>
            <FormItem label={"appKey"}>
                <Input placeholder={"appKey"} defaultValue={changyan.appKey}
                       onChange={(e) => setChangyan((prevState) => {
                           return {
                               ...prevState,
                               appKey: e.target.value,
                           }
                       })}/>
            </FormItem>
            <FormItem label={"回调地址"}>
                <Input placeholder={"回调地址"} defaultValue={changyan.callbackUrl}
                       onChange={(e) => setChangyan((prevState) => {
                           return {
                               ...prevState,
                               callbackUrl: e.target.value,
                           }
                       })}/>
            </FormItem>
        </Form>;
    }

    const onSubmit = async () => {
        setLoading(true);
        const params = new URLSearchParams();
        params.set("type", type);
        params.set("commentEmailNotify", (commentEmailNotify ? commentEmailNotify : false) + "")
        params.set("changyan", JSON.stringify(changyan));
        params.set("base", JSON.stringify(base));
        try {
            await axios.post("update", params.toString());
            messageApi.info("保存成功");
        } finally {
            setLoading(false);
        }
    }

    return (
        <Content style={{maxWidth: 600}}>
            {contextHolder}
            <Title style={{
                marginBottom: 0,
                fontWeight: 600,
                fontSize: "24px",
                lineHeight: 1.35,
                marginTop: "20px",
                borderLeft: "3px solid " + data.primaryColor,
                paddingLeft: "5px"
            }} level={3}>
                {data.plugin.name}设置
            </Title>
            <Divider/>
            <Form.Item label={"新评论邮件通知"}>
                <Switch defaultValue={commentEmailNotify} onChange={(e) => setCommentEmailNotify(e)}/>
            </Form.Item>
            <Form.Item label={"评论框类型"}>
                <Select onChange={setType} defaultValue={type} style={{maxWidth: 120}}>
                    <Option value={"base"}>默认</Option>
                    <Option value={"changyan"}>畅言</Option>
                </Select>
            </Form.Item>
            {getConfig()}
            <Divider/>
            <Button type="primary" loading={loading} htmlType="submit" onClick={async () => {
                await onSubmit()
            }}>
                提交
            </Button>
        </Content>
    );
};

export default CoreIndex;