import * as React from 'react';
import {Fragment} from 'react';
import {
    BulkDeleteButton,
    Button,
    Datagrid,
    Filter,
    FunctionField,
    List,
    TextField,
    TextInput,
    useListContext
} from 'react-admin';
import prettyBytes from 'pretty-bytes'
import {DownloadButton} from './DownloadButton'
import FolderIcon from '@material-ui/icons/Folder';
import InsertDriveFileIcon from '@material-ui/icons/InsertDriveFile';
import ExpandLessSharpIcon from '@material-ui/icons/ExpandLessSharp';
import ExpandMoreSharpIcon from '@material-ui/icons/ExpandMoreSharp';
import {makeStyles} from '@material-ui/core/styles';

const Filters = (props) => (
    <Filter {...props}>
        <TextInput label="相对目录" source="id" resettable allowEmpty alwaysOn/>
        <TextInput source="name" resettable allowEmpty alwaysOn/>
        {/*<NumberInput label={'大小下限'} source="sizes.lowerBound" resettable allowEmpty alwaysOn/>*/}
        {/*<NumberInput label={'大小上限'} source="sizes.upperBound" resettable allowEmpty alwaysOn/>*/}
    </Filter>
);

const BulkActionButtons = props => (
    <Fragment>
        <BulkDeleteButton {...props} undoable={false}/>
    </Fragment>
);

const UpButton = ({record}) => {
    const {
        filterValues,
        setFilters,
    } = useListContext();
    const getParent = path => {
        let parts = path.split('/');
        parts.pop();
        parts.pop();
        return parts.join('/');
    };
    return (
        record.id.indexOf('/') !== -1
            ? <Button label={'上级'} onClick={e => {
                e.stopPropagation();
                setFilters({...filterValues, id: getParent(record.id)})
            }}>
                <ExpandLessSharpIcon/>
            </Button>
            : null
    );
}
const DownButton = ({record}) => {
    const {
        filterValues,
        setFilters,
    } = useListContext();
    return (
        <Button label={'下级'} onClick={e => {
            e.stopPropagation();
            setFilters({...filterValues, id: record.id})
        }}>
            <ExpandMoreSharpIcon/>
        </Button>
    );
}

const OperateButton = ({record}) => record.folder
    ? <DownButton record={record}/>
    : <DownloadButton record={record}/>;

const FileTypeIcon = record => (record.folder ? <FolderIcon/> : <InsertDriveFileIcon/>);

const useListStyles = makeStyles({
    comment: {
        maxWidth: '18em',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
    },
});

export const FileList = props => {
    console.info('FileList:', props);
    const classes = useListStyles();
    return (
        <List {...props}
              filters={<Filters/>}
              bulkActionButtons={<BulkActionButtons/>}
              exporter={false}
        >
            <Datagrid rowClick={'toggleSelection'}>
                <FunctionField source={'folder'} render={FileTypeIcon}/>
                <TextField source="name" cellClassName={classes.comment}/>
                <TextField source="path" cellClassName={classes.comment}/>
                <FunctionField source={'sizes'} render={record => `${prettyBytes(record.sizes)}`}/>
                <UpButton/>
                <OperateButton/>
            </Datagrid>
        </List>
    )
};
