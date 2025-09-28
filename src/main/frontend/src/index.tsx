import {createRoot} from "react-dom/client";
import zh_CN from "antd/es/locale/zh_CN";
import {legacyLogicalPropertiesTransformer, StyleProvider} from "@ant-design/cssinjs";
import {useEffect, useState} from "react";
import {App, ConfigProvider, theme} from "antd";
import {BrowserRouter} from "react-router-dom";
import AppBase from "./AppBase";
import axios from "axios";

const {darkAlgorithm, defaultAlgorithm} = theme;

export interface PluginCoreInfoResponse {
    dark: boolean
    primaryColor: string
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
}

export interface PluginSetting {
    changyan: string;
    base: string;
    type: "changyan" | "base";
    commentEmailNotify: boolean;
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
        const a = document.getElementById("pluginInfo");
        if (a === null || a.innerText.length === 0) {
            return null;
        }
        return covertData(JSON.parse(a.innerText));
    } catch (e) {
        return null;
    }
}

const covertData = (data: PluginCoreInfoResponse) => {
    return data;
}

const Index = () => {
    const [pluginInfo, setPluginInfo] = useState<PluginCoreInfoResponse | null>(loadFromDocument);

    useEffect(() => {
        if (pluginInfo === null) {
            axios.get("json").then(({data}) => {
                setPluginInfo(covertData(data));
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
                algorithm: pluginInfo.dark ? darkAlgorithm : defaultAlgorithm,
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
            <BrowserRouter>
                <StyleProvider transformers={[legacyLogicalPropertiesTransformer]}>
                    <App>
                        <AppBase pluginInfo={pluginInfo}/>
                    </App>
                </StyleProvider>
            </BrowserRouter>
        </ConfigProvider>
    );
};

const container = document.getElementById("app");
const root = createRoot(container!); // createRoot(container!) if you use TypeScript
root.render(<Index/>);