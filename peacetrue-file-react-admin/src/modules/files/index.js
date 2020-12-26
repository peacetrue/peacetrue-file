import React from "react";
import {Resource} from "react-admin";
import {FileList} from './list';
import {FileCreate} from './create';
import InsertDriveFileIcon from '@material-ui/icons/InsertDriveFile';

export const File = {list: FileList, create: FileCreate};
const FileResource = <Resource icon={InsertDriveFileIcon} name="files" {...File} />;
export default FileResource;
