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

import { createGlobalStyle } from "styled-components";

const GlobalStyle = createGlobalStyle`
  body {
      background-color: #f5f7fa;
      color: #1f1f1f;
      transition: background-color 0.2s, color 0.2s;
  }
  body.dark {
      background-color: #141414;
      color: #dfdfdf;
  }
`;

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
    return data;
}

const Index = () => {
    const [pluginInfo, setPluginInfo] = useState<PluginCoreInfoResponse | null>(loadFromDocument);
    const [isDark, setIsDark] = useState<boolean>(pluginInfo?.dark || false);

    useEffect(() => {
        if (pluginInfo === null) {
            axios.get("json").then(({data}) => {
                setPluginInfo(covertData(data));
                setIsDark(data.dark);
            });
        }
    }, []);

    useEffect(() => {
        const observer = new MutationObserver(() => {
            const hasDark = document.body.classList.contains("dark");
            setIsDark(hasDark);
        });

        observer.observe(document.body, {
            attributes: true,
            attributeFilter: ["class"]
        });

        // Initial detection
        const hasDark = document.body.classList.contains("dark");
        setIsDark(hasDark);

        return () => {
            observer.disconnect();
        };
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
            <GlobalStyle />
            <BrowserRouter>
                <StyleProvider transformers={[legacyLogicalPropertiesTransformer]}>
                    <App>
                        <AppBase pluginInfo={{ ...pluginInfo, dark: isDark }}/>
                    </App>
                </StyleProvider>
            </BrowserRouter>
        </ConfigProvider>
    );
};

const container = document.getElementById("app");
const root = createRoot(container!); // createRoot(container!) if you use TypeScript
root.render(<Index/>);