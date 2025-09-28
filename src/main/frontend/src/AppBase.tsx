import {Route, Routes} from "react-router";
import CoreIndex from "./components/CoreIndex";
import {PluginCoreInfoResponse} from "./index";
import {FunctionComponent} from "react";

export type AppBaseProps = {
    pluginInfo: PluginCoreInfoResponse;
}

const AppBase: FunctionComponent<AppBaseProps> = ({pluginInfo}) => {

    return (
        <Routes>
            <Route path="*" element={<CoreIndex data={pluginInfo}/>}/>
        </Routes>
    );
}

export default AppBase;
