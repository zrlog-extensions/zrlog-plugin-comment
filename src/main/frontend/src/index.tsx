import {createRoot} from "react-dom/client";
import {ConfigProvider} from "antd";
import {useEffect, useState} from "react";
import {BrowserRouter} from "react-router-dom";
import AppBase from "./AppBase";
import axios from "axios";
import {PluginThemeProvider} from "./PluginThemeProvider";
import type {PluginAppearance} from "./PluginThemeProvider";

export interface PluginCoreInfoResponse extends PluginAppearance {
    theme?: string;
    primaryColor?: string;
    plugin: Plugin;
    adminToken: string;
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
    commentEmailNotify?: boolean | string;
    syncHistory?: string;
    moderation?: string;
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
        if (!a?.textContent) {
            return null;
        }
        return normalizeData(JSON.parse(a.textContent));
    } catch (e) {
        return null;
    }
}

const normalizeData = (data: PluginCoreInfoResponse) => {
    return {
        ...data,
        dark: data.dark ?? data.theme === "dark",
        colorPrimary: data.colorPrimary || data.primaryColor,
    };
}

const Index = () => {
    const [pluginInfo, setPluginInfo] = useState<PluginCoreInfoResponse | null>(loadFromDocument);

    useEffect(() => {
        if (pluginInfo === null) {
            axios.get<PluginCoreInfoResponse>("json").then(({data}) => {
                setPluginInfo(normalizeData(data));
            });
        }
    }, []);

    if (pluginInfo === null) {
        return <></>
    }

    return (
        <PluginThemeProvider page={pluginInfo}>
            <ConfigProvider divider={{style: {margin: "16px 0"}}} table={{style: {whiteSpace: "nowrap"}}}>
                <BrowserRouter>
                    <AppBase pluginInfo={pluginInfo}/>
                </BrowserRouter>
            </ConfigProvider>
        </PluginThemeProvider>
    );
};

const container = document.getElementById("app");
const root = createRoot(container!); // createRoot(container!) if you use TypeScript
root.render(<Index/>);
