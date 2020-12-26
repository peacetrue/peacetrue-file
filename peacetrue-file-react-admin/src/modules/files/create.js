import React from 'react';
import {Create, FileField, FileInput, required, SimpleForm, TextInput,} from 'react-admin';

import {formDataBuilder} from "./utils";

export const FileCreate = (props) => {
    console.info('FileCreate:', props);
    return (
        <Create {...props} transform={data => formDataBuilder(data, 'file')}>
            <SimpleForm>
                <TextInput source={'relativePath'} validate={[]}/>
                <FileInput label="文件" source="file"
                           minSize={1} maxSize={5000000}
                           validate={[required(),]}
                           placeholder={'点击或拖拽上传，支持最大 5M 的文件'}>
                    <FileField source="src" title="title"/>
                </FileInput>
            </SimpleForm>
        </Create>
    );
};
