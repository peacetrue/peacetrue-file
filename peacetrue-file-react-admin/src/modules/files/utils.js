import React from 'react';
import {ImageField, Labeled} from "react-admin";

export const formDataBuilder = (data) => {
    let name = "filePart", fileCount = 0;
    let formData = new FormData();
    Object.keys(data).forEach(key => {
        let value = data[key];
        //value=[{rawFile:File},{rawFile:File}]
        if (value instanceof Array) {
            value.forEach(item => {
                if (item.rawFile instanceof File) {
                    formData.append(name, item.rawFile, item.rawFile.name);
                } else {
                    throw new Error("Unexpected");
                }
            });
        } else if (value.rawFile instanceof File) {
            fileCount++;
            formData.append(name, value.rawFile, value.rawFile.name);
        } else {
            formData.append(key, value);
        }
    });

    formData.append("_query", JSON.stringify({fileCount: fileCount}));
    return formData;
}

export const fileParamBuilder = (name, relativePath, single = true) => {
    return {name, relativePath, type: single ? 'single' : 'multiple', single};
}

export const fileFormDataBuilder = ({relativePath, files, type = 'single'}) => {
    const name = "filePart";
    let formData = new FormData();
    formData.append('relativePath', relativePath);

    if (type === 'single') {
        formData.append(name, files, files.name);
    } else {
        files.forEach(file => formData.append(name, file, file.name));
    }
    formData.append("_query", JSON.stringify({'type': type}))
    return formData;
}

export const extractFiles = (data, fileParam) => {
    let value = data[fileParam.name];
    //nothing change: "teacher/pc-photo/865-手机(8).jpeg,teacher/pc-photo/976-手机(13).png"

    if (!value) return null;
    if (typeof value === 'string') return null;
    return fileParam.single ? value.rawFile : value.map(item => item.rawFile).filter(item => Boolean(item));
}

export const isEmpty = value => {
    if (!value) return true;
    if (value.length === 0) return true;
    return false;
}

export const fileTransformBuilder = (dataProvider, fileParams) => {
    return data => {
        console.info("transform.data:", data);
        fileParams = fileParams
            .map(fileParam => extractFiles(data, fileParam))
            .map((files, index) => ({...fileParams[index], files}))
            .filter(fileParam => !isEmpty(fileParam.files));
        let promises = fileParams
            .map(fileParam => fileFormDataBuilder(fileParam))
            .map(file => dataProvider.create('files', {data: file}));
        return Promise.all(promises)
            .then(responses => {
                console.info("responses:", responses);
                let transformedData = {...data};
                responses.forEach((response, index) => {
                    if (!response) return;//会话超时，此时无结果
                    let files = response.data, fileParam = fileParams[index];
                    if (files instanceof Array) {
                        let values = data[fileParam.name], newIndex = 0;
                        values.forEach((value, index) => {
                            if (value.rawFile) values[index] = files[newIndex++].id;
                            else values[index] = value.value;
                        });
                        transformedData[fileParam.name] = values.join(',');
                    } else {
                        transformedData[fileParam.name] = files.id;
                    }
                });
                return transformedData;
            })
    };
}

export const buildUrl = (path, dispositionType) => `${process.env.REACT_APP_BASE_URL}/files/${path}?dispositionType=${dispositionType}`;
export const buildPreviewUrl = path => buildUrl(path, 'inline');
export const buildDownloadUrl = path => buildUrl(path, 'attachment');

export const filePathFormatter = item => {
    return {
        value: item,
        src: buildDownloadUrl(item),
        title: item.substring(item.lastIndexOf('/') + 1)
    };
}

export const inputFormatterBuilder = single => {
    return (data) => {
        if (typeof data === 'string') {
            if (single) {
                //single path: teacher/photo.png
                return filePathFormatter(data);
            } else {
                //multiple path: teacher/photo1.png,teacher/photo2.png
                return data.split(',').map(item => filePathFormatter(item));
            }
        }
        //File(name=photo.png,path=photo.png)
        return data;
    }
}

export const multipleInputFormatter = (data) => {
    if (typeof data === 'string') {
        //multiple path: teacher/photo1.png,teacher/photo2.png
        return data.split(',').map(item => filePathFormatter(item));
    }
    //File(name=photo.png,path=photo.png)
    return data;
}

export const singleInputFormatter = (data) => {
    if (typeof data === 'string') {
        //multiple path: teacher/photo1.png,teacher/photo2.png
        return filePathFormatter(data);
    }
    //File(name=photo.png,path=photo.png)
    return data;
}

export const inputParser = (input) => {
    // create
    // 1. data => File
    if (input instanceof File) {
        return {src: URL.createObjectURL(input), title: input.path, rawFile: input};
    }

    if (typeof input === 'string') {
        let index = input.lastIndexOf('/');
        return {src: buildDownloadUrl(input), title: input.substring(index + 1)};
    }
    return input;
}

export const labeledImageFieldBuilder = (resources) => {
    return ({resource, label, source, record, ...rest}) => (
        <Labeled label={label || resources[resource].fields[source]}>
            <ImageField  {...rest} resource={resource} source={source}
                         record={{[source]: buildDownloadUrl(record[source])}}/>
        </Labeled>
    );
};

export const CustomImageField = ({source, record, ...rest}) => (
    <ImageField  {...rest} source={source} record={{[source]: buildDownloadUrl(record[source])}}/>
);


