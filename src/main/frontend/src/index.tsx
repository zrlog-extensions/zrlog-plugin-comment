import {createRoot} from "react-dom/client";
import zh_CN from "antd/es/locale/zh_CN";
import {legacyLogicalPropertiesTransformer, StyleProvider} from "@ant-design/cssinjs";
import {useEffect, useState} from "react";
import {App, ConfigProvider, Layout, theme} from "antd";
import {BrowserRouter} from "react-router-dom";
import AppBase from "./AppBase";
import axios from "axios";

const {darkAlgorithm, defaultAlgorithm} = theme;
const {Content} = Layout;

export interface PluginCoreInfoResponse {
    dark: boolean
    theme?: string
    primaryColor: string
    colorPrimary?: string
    plugin: Plugin;
    setting: PluginSetting
}

export interface ChangyanSetting {
    appKey: string;
    appId: string;
    callbackUrl: string;
}

export interface BaseSetting {
    styleStr: string;
    baseUrl: string;
    mainColor: string;
}

export interface PluginSetting {
    changyan: string;
    base: string;
    type: "changyan" | "base";
    commentEmailNotify: boolean;
    syncHistory?: string;
}

export interface Plugin {
    id: string
    version: string
    name: string
    paths: string[]
    actions: any[]
    desc: string
    author: string
    shortName: string
    indexPage: string
    previewImageBase64: string
    services: string[]
    dependentService: string[]
}

const loadFromDocument = () => {
    try {
        const a = document.getElementById("data") || document.getElementById("pluginInfo");
        if (a === null || a.innerText.length === 0) {
            return null;
        }
        return covertData(JSON.parse(a.innerText));
    } catch (e) {
        return null;
    }
}

const covertData = (data: PluginCoreInfoResponse) => {
    return {
        ...data,
        dark: data.dark ?? data.theme === "dark",
        primaryColor: data.primaryColor || data.colorPrimary || "#1677ff",
    };
}

const IndexContent = ({pluginInfo, isDark}: { pluginInfo: PluginCoreInfoResponse; isDark: boolean }) => {
    return (
        <BrowserRouter>
            <StyleProvider transformers={[legacyLogicalPropertiesTransformer]}>
                <Content>
                    <App>
                        <AppBase pluginInfo={{ ...pluginInfo, dark: isDark }}/>
                    </App>
                </Content>
            </StyleProvider>
        </BrowserRouter>
    );
};

const Index = () => {
    const [pluginInfo, setPluginInfo] = useState<PluginCoreInfoResponse | null>(loadFromDocument);
    const isDark = pluginInfo?.dark || false;

    useEffect(() => {
        if (pluginInfo === null) {
            axios.get("json").then(({data}) => {
                const nextPluginInfo = covertData(data);
                setPluginInfo(nextPluginInfo);
            });
        }
    }, []);

    if (pluginInfo === null) {
        return <></>
    }

    return (
        <ConfigProvider
            locale={zh_CN}
            theme={{
                algorithm: isDark ? darkAlgorithm : defaultAlgorithm,
                token: {
                    colorPrimary: pluginInfo.primaryColor
                }
            }}
            divider={{
                style: {
                    margin: "16px 0px"
                }
            }}
            table={
                {
                    style: {
                        whiteSpace: "nowrap"
                    },
                }}
        >
            <IndexContent pluginInfo={pluginInfo} isDark={isDark}/>
        </ConfigProvider>
    );
};

const container = document.getElementById("app");
const root = createRoot(container!); // createRoot(container!) if you use TypeScript
root.render(<Index/>);
