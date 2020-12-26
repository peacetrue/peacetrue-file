import React from 'react';
import {BooleanField, FunctionField, Show, SimpleShowLayout, TextField, TopToolbar} from 'react-admin';
import {DownloadButton} from "./DownloadButton";
import prettyBytes from "pretty-bytes";

const FileActions = ({basePath, data, resource}) => (
    <TopToolbar>
        {/*<ListButton basePath={basePath} record={data}/>*/}
        <DownloadButton filePath={data.path}/>
    </TopToolbar>
);

export const FileShow = (props) => {
    console.info('FileShow:', props);
    return (
        <Show actions={<FileActions/>} {...props} >
            <SimpleShowLayout>
                <BooleanField label={'目录'} source="folder"/>
                <TextField label={'名称'} source="name"/>
                <TextField label={'路径'} source="path"/>
                <FunctionField label={'大小（字节）'} render={record => `${prettyBytes(record.sizes)}`}/>
            </SimpleShowLayout>
        </Show>
    );
};
