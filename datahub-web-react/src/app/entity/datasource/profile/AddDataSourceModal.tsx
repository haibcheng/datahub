import { DeleteOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, Modal, Space, Select, Alert, Switch } from 'antd';
import React, { useState } from 'react';
import { FormField, IDatasourceSourceInput, IFormConnectionData, IFormData } from '../service/DataSourceType';
import { showMessageByNotification, showRequestResult } from '../service/NotificationUtil';
import {
    sourceTypeList,
    DbSourceTypeData,
    groupList as defaultGroupList,
    dataCenterList,
    regionList,
} from '../service/FormInitValue';
import { useCreateDatasourceMutation, useTestDatasourceMutation } from '../../../../graphql/datasource.generated';
import { CorpGroup, DatasourceCreateInput } from '../../../../types.generated';
import { useGetUserQuery } from '../../../../graphql/user.generated';
import { Message } from '../../../shared/Message';

const messageStyle = { marginTop: '10%' };

const { Option } = Select;

type AddDataSourceModalProps = {
    visible: boolean;
    onClose: () => void;
    title: string;
    originData?: any;
    corpUserUrn: any;
};

const layout = {
    labelCol: { span: 4 },
    wrapperCol: { span: 20 },
};
export default function AddDataSourceModal({
    visible,
    onClose,
    title,
    originData,
    corpUserUrn,
}: AddDataSourceModalProps) {
    let count = 1; // when originData exists ,show the edit
    const [createDatasourceMutation] = useCreateDatasourceMutation();
    const [testDatasourceMutation] = useTestDatasourceMutation();
    const urn = corpUserUrn;
    const { loading, error, data } = useGetUserQuery({ variables: { urn, groupsCount: 20 } });

    const relationships = data?.corpUser?.groups;
    const groupList = relationships?.relationships?.map((rel) => rel.entity as CorpGroup) || defaultGroupList;

    const [saveLoading, updateLoading] = useState(false);
    const [testLoading, updateTestLoading] = useState(false);

    const initData: IFormData = originData ?? {
        sourceType: sourceTypeList[0].value,
        name: '',
        alias: '',
        status: '',
        testQuerySql: '',
        create: true,
        group: groupList[0]?.urn,
        region: regionList[0]?.value,
        connections: [
            {
                id: 1,
                username: '',
                password: '',
                hostPort: '',
                bootstrap: '',
                minSize: 20,
                maxSize: 20,
                idleSize: 20,
                status: '',
                schemaPatternAllow: '',
                tablePatternAllow: '',
                topicPatternsAllow: '',
                hiveMetastoreUris: '',
                dataCenter: dataCenterList[0]?.value,
            },
        ],
    };

    const [formData, updateDataSourceFormData] = useState(initData);
    const [form] = Form.useForm();

    if (error || (!loading && !error && !data)) {
        return <Alert type="error" message={error?.message || 'Entity failed to load'} />;
    }

    if (!formData.create) {
        formData.group = originData.group;
        console.log('originData====', originData);
    } else if (formData.create && formData.group === 'urn:li:corpGroup:none') {
        console.log('formData.group111====', formData.group, groupList[0]?.urn, groupList);
        formData.group = groupList[0]?.urn;
    }

    const showValidateMsg = (msg) => {
        showMessageByNotification(msg);
    };

    const databaseRequired =
        formData.sourceType === DbSourceTypeData.TiDB ||
        formData.sourceType === DbSourceTypeData.Postgres ||
        formData.sourceType === DbSourceTypeData.Hive ||
        formData.sourceType === DbSourceTypeData.Mysql;

    const enableSync =
        formData.sourceType === DbSourceTypeData.Oracle ||
        formData.sourceType === DbSourceTypeData.TiDB ||
        formData.sourceType === DbSourceTypeData.Mysql ||
        formData.sourceType === DbSourceTypeData.Snowflake ||
        formData.sourceType === DbSourceTypeData.Pinot ||
        formData.sourceType === DbSourceTypeData.Postgres ||
        formData.sourceType === DbSourceTypeData.Hive ||
        formData.sourceType === DbSourceTypeData.presto ||
        formData.sourceType === DbSourceTypeData.trino;

    const isInKafka = () => {
        return formData.sourceType === DbSourceTypeData.Kafka;
    };

    const isIceBerge = () => {
        return formData.sourceType === DbSourceTypeData.Iceberg;
    };

    const isOracle = () => {
        return formData.sourceType === DbSourceTypeData.Oracle;
    };

    const isTrino = () => {
        return formData.sourceType === DbSourceTypeData.trino;
    };

    const isPinot = () => {
        return formData.sourceType === DbSourceTypeData.Pinot;
    };

    const isPresto = () => {
        return formData.sourceType === DbSourceTypeData.presto;
    };

    const isTiDB = () => {
        return formData.sourceType === DbSourceTypeData.TiDB;
    };

    const isHive = () => {
        return formData.sourceType === DbSourceTypeData.Hive;
    };

    const isMysql = () => {
        return formData.sourceType === DbSourceTypeData.Mysql;
    };

    const isSnowflake = () => {
        return formData.sourceType === DbSourceTypeData.Snowflake;
    };

    const isPostgres = () => {
        return formData.sourceType === DbSourceTypeData.Postgres;
    };

    const checkFormData = () => {
        if (!formData) {
            return false;
        }
        const { sourceType, name, group, region } = formData;
        const isBasicOK = !!sourceType && !!name && !!group && !!region;
        let isOk = isBasicOK;
        if (!isBasicOK) {
            return false;
        }
        if (isIceBerge()) {
            isOk = !formData.connections?.some((item) => {
                return item.hiveMetastoreUris === '';
            });
        } else if (isInKafka()) {
            isOk = !formData.connections?.some((item) => {
                return item.bootstrap === '';
            });
        } else if (isPinot() || isTrino() || isPresto() || isSnowflake()) {
            isOk = !formData.connections?.some((item) => {
                return item.username === '' || item.password === '' || item.hostPort === '';
            });
        } else if (isHive() || isMysql() || isTiDB() || isPostgres()) {
            isOk = !formData.connections?.some((item) => {
                return item.username === '' || item.password === '' || item.hostPort === '' || item.database === '';
            });
        } else if (isOracle()) {
            isOk = !formData.connections?.some((item) => {
                return (
                    item.username === '' ||
                    item.password === '' ||
                    ((item.hostPort === '' || item.serviceName === '') && item.tnsName === '')
                );
            });
        } else {
            isOk = !formData.connections?.some((item) => {
                return item.username === '' || item.password === '' || item.hostPort === '';
            });
        }
        return isOk;
    };

    const getDataSourceInputData = () => {
        const dataSources: IDatasourceSourceInput[] = formData.connections?.map((conn) => {
            const dataSource: IDatasourceSourceInput = {
                dataCenter: conn.dataCenter,
            };
            switch (formData.sourceType) {
                case DbSourceTypeData.Iceberg: {
                    dataSource[`${formData.sourceType}`] = {
                        hiveMetastoreUris: conn.hiveMetastoreUris,
                    };
                    break;
                }
                case DbSourceTypeData.Kafka: {
                    dataSource[`${formData.sourceType}`] = {
                        bootstrap: conn.bootstrap,
                        topicPatternsAllow: conn.topicPatternsAllow,
                    };
                    if (conn.schemaRegistryUrl !== '') {
                        dataSource[`${formData.sourceType}`] = {
                            ...dataSource[`${formData.sourceType}`],
                            schemaRegistryUrl: conn.schemaRegistryUrl,
                        };
                    }
                    break;
                }
                case DbSourceTypeData.Mysql:
                case DbSourceTypeData.Postgres:
                case DbSourceTypeData.TiDB:
                case DbSourceTypeData.Hive: {
                    dataSource[`${formData.sourceType}`] = {
                        username: conn.username,
                        password: conn.password,
                        hostPort: conn.hostPort,
                        database: conn.database,
                        minSize: conn.minSize,
                        maxSize: conn.maxSize,
                        idleSize: conn.idleSize,
                        status: conn.status,
                        tablePatternAllow: conn.tablePatternAllow,
                        schemaPatternAllow: conn.schemaPatternAllow,
                    };
                    if (conn.jdbcParams !== '') {
                        dataSource[`${formData.sourceType}`] = {
                            ...dataSource[`${formData.sourceType}`],
                            jdbcParams: conn.jdbcParams,
                        };
                    }
                    break;
                }
                case DbSourceTypeData.trino:
                case DbSourceTypeData.presto: {
                    dataSource[`${formData.sourceType}`] = {
                        username: conn.username,
                        password: conn.password,
                        hostPort: conn.hostPort,
                        minSize: conn.minSize,
                        maxSize: conn.maxSize,
                        idleSize: conn.idleSize,
                        status: conn.status,
                        tablePatternAllow: conn.tablePatternAllow,
                        schemaPatternAllow: conn.schemaPatternAllow,
                    };
                    if (conn.catalog !== '') {
                        dataSource[`${formData.sourceType}`] = {
                            ...dataSource[`${formData.sourceType}`],
                            catalog: conn.catalog,
                        };
                    }
                    if (conn.schema !== '') {
                        dataSource[`${formData.sourceType}`] = {
                            ...dataSource[`${formData.sourceType}`],
                            schema: conn.schema,
                        };
                    }
                    if (conn.jdbcParams !== '') {
                        dataSource[`${formData.sourceType}`] = {
                            ...dataSource[`${formData.sourceType}`],
                            jdbcParams: conn.jdbcParams,
                        };
                    }
                    break;
                }
                case DbSourceTypeData.Oracle: {
                    dataSource[`${formData.sourceType}`] = {
                        username: conn.username,
                        password: conn.password,
                        minSize: conn.minSize,
                        maxSize: conn.maxSize,
                        idleSize: conn.idleSize,
                        status: conn.status,
                        tablePatternAllow: conn.tablePatternAllow,
                        schemaPatternAllow: conn.schemaPatternAllow,
                    };
                    if (conn.hostPort !== '') {
                        dataSource[`${formData.sourceType}`] = {
                            ...dataSource[`${formData.sourceType}`],
                            hostPort: conn.hostPort,
                        };
                    }
                    if (conn.serviceName !== '') {
                        dataSource[`${formData.sourceType}`] = {
                            ...dataSource[`${formData.sourceType}`],
                            serviceName: conn.serviceName,
                        };
                    }
                    if (conn.tnsName !== '') {
                        dataSource[`${formData.sourceType}`] = {
                            ...dataSource[`${formData.sourceType}`],
                            tnsName: conn.tnsName,
                        };
                    }
                    break;
                }
                case DbSourceTypeData.Pinot: {
                    dataSource[`${formData.sourceType}`] = {
                        username: conn.username,
                        password: conn.password,
                        hostPort: conn.hostPort,
                        minSize: conn.minSize,
                        maxSize: conn.maxSize,
                        idleSize: conn.idleSize,
                        status: conn.status,
                        tablePatternAllow: conn.tablePatternAllow,
                        schemaPatternAllow: conn.schemaPatternAllow,
                    };
                    break;
                }
                case DbSourceTypeData.Snowflake: {
                    dataSource[`${formData.sourceType}`] = {
                        username: conn.username,
                        password: conn.password,
                        hostPort: conn.hostPort,
                        tablePatternAllow: conn.tablePatternAllow,
                        schemaPatternAllow: conn.schemaPatternAllow,
                    };
                    if (conn.connectionParams !== '') {
                        dataSource[`${formData.sourceType}`] = {
                            ...dataSource[`${formData.sourceType}`],
                            connectionParams: conn.connectionParams,
                        };
                    }
                    break;
                }
                default: {
                    break;
                }
            }
            return dataSource;
        });
        return dataSources;
    };

    const onTestBtnClick = (ix: number) => {
        // check form data
        updateTestLoading(true);

        let conn: IDatasourceSourceInput = {
            dataCenter: formData.connections[ix].dataCenter,
        };

        switch (formData.sourceType) {
            case DbSourceTypeData.Postgres: {
                conn = {
                    ...conn,
                    postgres: {
                        username: formData.connections[ix].username || '',
                        password: formData.connections[ix].password || '',
                        hostPort: formData.connections[ix].hostPort || '',
                        database: formData.connections[ix].database || '',
                        jdbcParams: formData.connections[ix].jdbcParams || '',
                    },
                };
                break;
            }
            case DbSourceTypeData.Mysql: {
                conn = {
                    ...conn,
                    mysql: {
                        username: formData.connections[ix].username || '',
                        password: formData.connections[ix].password || '',
                        hostPort: formData.connections[ix].hostPort || '',
                        database: formData.connections[ix].database || '',
                        jdbcParams: formData.connections[ix].jdbcParams || '',
                    },
                };
                break;
            }
            case DbSourceTypeData.Hive: {
                conn = {
                    ...conn,
                    hive: {
                        username: formData.connections[ix].username || '',
                        password: formData.connections[ix].password || '',
                        hostPort: formData.connections[ix].hostPort || '',
                        database: formData.connections[ix].database || '',
                        jdbcParams: formData.connections[ix].jdbcParams || '',
                    },
                };
                break;
            }
            case DbSourceTypeData.TiDB: {
                conn = {
                    ...conn,
                    tiDB: {
                        username: formData.connections[ix].username || '',
                        password: formData.connections[ix].password || '',
                        hostPort: formData.connections[ix].hostPort || '',
                        database: formData.connections[ix].database || '',
                        jdbcParams: formData.connections[ix].jdbcParams || '',
                    },
                };
                break;
            }
            case DbSourceTypeData.Pinot: {
                conn = {
                    ...conn,
                    pinot: {
                        username: formData.connections[ix].username || '',
                        password: formData.connections[ix].password || '',
                        hostPort: formData.connections[ix].hostPort || '',
                    },
                };
                break;
            }
            case DbSourceTypeData.presto: {
                conn = {
                    ...conn,
                    presto: {
                        username: formData.connections[ix].username || '',
                        password: formData.connections[ix].password || '',
                        hostPort: formData.connections[ix].hostPort || '',
                        catalog: formData.connections[ix].catalog || '',
                        schema: formData.connections[ix].schema || '',
                        jdbcParams: formData.connections[ix].jdbcParams || '',
                    },
                };
                break;
            }
            case DbSourceTypeData.trino: {
                conn = {
                    ...conn,
                    trino: {
                        username: formData.connections[ix].username || '',
                        password: formData.connections[ix].password || '',
                        hostPort: formData.connections[ix].hostPort || '',
                        catalog: formData.connections[ix].catalog || '',
                        schema: formData.connections[ix].schema || '',
                        jdbcParams: formData.connections[ix].jdbcParams || '',
                    },
                };
                break;
            }
            case DbSourceTypeData.Oracle: {
                conn = {
                    ...conn,
                    oracle: {
                        username: formData.connections[ix].username || '',
                        password: formData.connections[ix].password || '',
                        hostPort: formData.connections[ix].hostPort || '',
                        serviceName: formData.connections[ix].serviceName || '',
                        tnsName: formData.connections[ix].tnsName || '',
                    },
                };
                break;
            }
            default: {
                break;
            }
        }

        const input = {
            testQuerySql: formData.testQuerySql,
            connection: {
                ...conn,
            },
        };

        testDatasourceMutation({
            variables: {
                input,
            },
        })
            .then((res) => {
                console.log('testDatasourceMutation res....', res, input);
                if (!res) {
                    showRequestResult(500, 'Failed', true);
                    return;
                }
                if (res?.data?.testDatasource === true) {
                    showRequestResult(200, 'Success', true);
                } else {
                    showRequestResult(500, 'Failed', true);
                }
            })
            .catch((err) => {
                console.log('testDatasourceMutation error....', err, input);
                showRequestResult(500, 'Failed', true);
            })
            .finally(() => {
                updateTestLoading(false);
            });
    };

    const onSaveBtnClick = () => {
        const isOk = checkFormData();
        if (!isOk) {
            showValidateMsg('Exist some required value missing from form items !');
            return;
        }
        updateLoading(true);
        const dataSources: IDatasourceSourceInput[] = getDataSourceInputData();
        let input: DatasourceCreateInput = {
            name: formData.name,
            alias: formData.alias,
            testQuerySql: formData.testQuerySql,
            create: formData.create,
            primaryConn: dataSources[0],
            group: formData.group,
            region: formData.region,
        };
        if (dataSources?.length > 1) {
            input = {
                ...input,
                gsbConn: dataSources[1],
            };
        }

        createDatasourceMutation({
            variables: {
                input,
            },
        })
            .then((res) => {
                console.log('createDatasourceMutation res....', res, input);
                const errors = res?.errors;
                if (errors) {
                    showRequestResult(500);
                    return;
                }
                onClose();
                showRequestResult(200);
                try {
                    localStorage.setItem('datahub.latestDataSource', JSON.stringify(input));
                } catch (e) {
                    console.log('save latest data source error', e);
                }
            })
            .catch((err) => {
                console.log('createDatasourceMutation error....', err, input);
                showRequestResult(500);
            })
            .finally(() => {
                updateLoading(false);
            });
    };

    const onCancelBtnClick = () => {
        updateDataSourceFormData(initData);
        onClose();
    };

    const onAddMoreBtnClick = () => {
        const info: IFormConnectionData = {
            id: ++count,
            username: '',
            password: '',
            hostPort: '',
            bootstrap: '',
            minSize: 20,
            maxSize: 20,
            idleSize: 20,
            status: '',
            schemaPatternAllow: '',
            tablePatternAllow: '',
            topicPatternsAllow: '',
            hiveMetastoreUris: '',
            dataCenter: dataCenterList[0]?.value,
        };

        const connections = [...formData.connections];
        connections.push(info);

        const updatedFormData = {
            ...formData,
            connections,
        };
        updateDataSourceFormData(updatedFormData);
    };

    const removeConnectionItem = (index: number) => {
        const { connections } = formData;
        count--;
        if (count <= 1) {
            count = 1;
        }
        const filterConns = connections?.filter((item: IFormConnectionData, ix: number) => {
            if (ix === index) {
                return false;
            }
            return true;
        });
        const updatedFormData = {
            ...formData,
            connections: filterConns,
        };
        updateDataSourceFormData(updatedFormData);
    };

    const updateDataSourceConnections = (value: any, field: FormField, ix: number) => {
        const updatedData = {
            ...formData,
        };
        const { connections } = updatedData;
        const item = connections[ix];
        if (!item) {
            return;
        }
        console.log('updateDataSourceConnections field....', field, item[field], value);
        console.log('formData field....', formData, formData.connections[ix]);
        // if (isOracle() && field === FormField.serviceName) {
        //     if (value === '') {
        //         if (formData.connections[ix].hostPort === '') {
        //             if (formData.connections[ix].tnsName === '') {
        //                 isTns = '';
        //             } else {
        //                 isTns = 'true';
        //             }
        //         } else {
        //             isTns = 'false';
        //         }
        //     } else {
        //         isTns = 'false';
        //     }
        // }
        // if (isOracle() && field === FormField.hostPort) {
        //     if (value === '') {
        //         if (formData.connections[ix].serviceName === '') {
        //             if (formData.connections[ix].tnsName === '') {
        //                 isTns = '';
        //             } else {
        //                 isTns = 'true';
        //             }
        //         } else {
        //             isTns = 'false';
        //         }
        //     } else {
        //         isTns = 'false';
        //     }
        // }
        // if (isOracle() && field === FormField.tnsName) {
        //     if (value === '') {
        //         if (formData.connections[ix].serviceName === '' && formData.connections[ix].hostPort === '') {
        //             isTns = '';
        //         } else {
        //             isTns = 'false';
        //         }
        //     } else {
        //         isTns = 'true';
        //     }
        // }
        item[field] = value;
        if (isOracle() && (field === FormField.serviceName || field === FormField.hostPort)) {
            item[FormField.tnsName] = '';
        }
        if (isOracle() && field === FormField.tnsName) {
            item[FormField.serviceName] = '';
            item[FormField.hostPort] = '';
        }
        updateDataSourceFormData(updatedData);
    };

    const onChange = (checked: boolean, index: number) => {
        console.log(`switch to ${checked}`);
        if (checked) {
            updateDataSourceConnections('1', FormField.status, index);
        } else {
            updateDataSourceConnections('0', FormField.status, index);
        }
    };

    const updateDataSourceBasicInfo = (value: any, field: FormField) => {
        const updateInfo = {};
        updateInfo[field] = value;
        const updatedData = {
            ...formData,
            ...updateInfo,
        };
        updateDataSourceFormData(updatedData);
    };

    const dataCenterChangeHandler = (value: any, field: FormField, ix: number) => {
        updateDataSourceConnections(value, field, ix);
    };

    const selectChangeHandler = (value: any, field) => {
        const updateInfo = {};
        updateInfo[field] = value;
        console.log('Group----', updateInfo[field], field, value);
        const updatedData = {
            ...formData,
            ...updateInfo,
        };
        updateDataSourceFormData(updatedData);
        formData[field] = value;
        console.log('Group----', formData, formData.group, formData.sourceType);
    };

    const getConnectionTitle = (index) => {
        return index < 1 ? 'Connection (Primary)' : 'Connection (GSB)';
    };

    const groupOptions = groupList?.map((item) => {
        return (
            <Option key={item.urn} value={item.urn}>
                {item.info?.displayName}
            </Option>
        );
    });

    const regionOptions = regionList?.map((item) => {
        return (
            <Option key={item.value} value={item.value}>
                {item.label}
            </Option>
        );
    });

    const sourceTypeOptions = sourceTypeList?.map((item) => {
        return (
            <Option key={item.value} value={item.value}>
                {item.label}
            </Option>
        );
    });

    const dataCenterOptions = dataCenterList?.map((item) => {
        return (
            <Option key={item.value} value={item.value}>
                {item.label}
            </Option>
        );
    });

    const dataSourceBasic = () => {
        return (
            <Card title="Data Source">
                <Form.Item label="Type" rules={[{ required: true, message: 'Please input dataSource type!' }]}>
                    <Select
                        disabled={!formData.create}
                        defaultValue={formData.sourceType}
                        onChange={(value) => {
                            selectChangeHandler(value, FormField.sourceType);
                        }}
                    >
                        {sourceTypeOptions}
                    </Select>
                </Form.Item>
                <Form.Item
                    name="group"
                    label="Group"
                    rules={[{ required: true, message: 'Please choose dataSource Group!' }]}
                >
                    <Select
                        disabled={!formData.create}
                        defaultValue={formData.group}
                        onChange={(value) => {
                            selectChangeHandler(value, FormField.group);
                        }}
                    >
                        {groupOptions}
                    </Select>
                </Form.Item>
                <Form.Item
                    name="region"
                    label="Region"
                    rules={[{ required: true, message: 'Please choose dataSource Region!' }]}
                >
                    <Select
                        disabled={!formData.create}
                        defaultValue={formData.region}
                        onChange={(value) => {
                            selectChangeHandler(value, FormField.region);
                        }}
                    >
                        {regionOptions}
                    </Select>
                </Form.Item>
                <Form.Item
                    name="name"
                    label="Name"
                    rules={[{ required: true, message: 'Please input dataSource name!' }]}
                >
                    <Input
                        disabled={!formData.create}
                        placeholder="Please input dataSource name"
                        autoComplete="off"
                        defaultValue={formData.name}
                        onChange={(e) => updateDataSourceBasicInfo(e.target.value, FormField.name)}
                    />
                </Form.Item>
                <Form.Item
                    name="alias"
                    label="Alias"
                    rules={[{ required: false, message: 'Please input dataSource alias!' }]}
                >
                    <Input
                        // disabled={!formData.create}
                        placeholder="Please input dataSource alias"
                        autoComplete="off"
                        defaultValue={formData.alias}
                        onChange={(e) => updateDataSourceBasicInfo(e.target.value, FormField.alias)}
                    />
                </Form.Item>
                <Form.Item
                    name="testQuerySql"
                    label="Test Sql"
                    rules={[{ required: false, message: 'Please input test query sql!' }]}
                >
                    <Input
                        // disabled={!formData.create}
                        placeholder="Please input test query sql"
                        autoComplete="off"
                        defaultValue={formData.testQuerySql}
                        onChange={(e) => updateDataSourceBasicInfo(e.target.value, FormField.testQuerySql)}
                    />
                </Form.Item>
            </Card>
        );
    };

    const getIceBergeConnection = (params: IFormConnectionData[]) => {
        return params.map((info: IFormConnectionData, index: number) => {
            return (
                <Card
                    style={{ marginTop: 16 }}
                    type="inner"
                    size="small"
                    title={getConnectionTitle(index)}
                    extra={index >= 1 && <DeleteOutlined onClick={() => removeConnectionItem(index)} />}
                    key={info.id}
                >
                    <Space direction="vertical" style={{ width: '100%', marginTop: 0 }}>
                        <Form.Item
                            name={`hiveUri_${info.id}`}
                            label="Uri"
                            rules={[{ required: true, message: 'Please input connection hive meta store uri!' }]}
                        >
                            <Input
                                placeholder="Please input connection hive meta store uri"
                                autoComplete="off"
                                defaultValue={info.hiveMetastoreUris}
                                onChange={(e) =>
                                    updateDataSourceConnections(e.target.value, FormField.hiveMetastoreUris, index)
                                }
                            />
                        </Form.Item>
                        <Form.Item
                            name={`dataCenter_${info.id}`}
                            label="Data Center"
                            rules={[{ required: false, message: 'Please input connection data center!' }]}
                        >
                            <Select
                                defaultValue={info.dataCenter}
                                onChange={(value) => {
                                    dataCenterChangeHandler(value, FormField.dataCenter, index);
                                }}
                            >
                                {dataCenterOptions}
                            </Select>
                        </Form.Item>
                    </Space>
                </Card>
            );
        });
    };

    const getKafkaConnection = (params: IFormConnectionData[]) => {
        return params.map((info: IFormConnectionData, index: number) => {
            return (
                <Card
                    style={{ marginTop: 16 }}
                    type="inner"
                    size="small"
                    title={getConnectionTitle(index)}
                    extra={index >= 1 && <DeleteOutlined onClick={() => removeConnectionItem(index)} />}
                    key={info.id}
                >
                    <Space direction="vertical" style={{ width: '100%', marginTop: 0 }}>
                        <Form.Item
                            name={`dataCenter_${info.id}`}
                            label="Data Center"
                            rules={[{ required: false, message: 'Please input connection data center!' }]}
                        >
                            <Select
                                defaultValue={info.dataCenter}
                                onChange={(value) => {
                                    dataCenterChangeHandler(value, FormField.dataCenter, index);
                                }}
                            >
                                {dataCenterOptions}
                            </Select>
                        </Form.Item>
                        <Form.Item
                            name={`bootstrapServer_${info.id}`}
                            label="Bootstrap Server"
                            rules={[{ required: true, message: 'Please input connection Bootstrap Server!' }]}
                        >
                            <Input
                                placeholder="Please input connection bootstrap Server"
                                autoComplete="off"
                                defaultValue={info.bootstrap}
                                onChange={(e) =>
                                    updateDataSourceConnections(e.target.value, FormField.bootstrap, index)
                                }
                            />
                        </Form.Item>
                        <Form.Item
                            name={`schemaRegistryURL_${info.id}`}
                            label="Schema Registry URL"
                            rules={[{ required: false, message: 'Please input connection Schema Registry URL!' }]}
                        >
                            <Input
                                placeholder="Please input connection bootstrap Server"
                                autoComplete="off"
                                defaultValue={info.bootstrap}
                                onChange={(e) =>
                                    updateDataSourceConnections(e.target.value, FormField.schemaRegistryUrl, index)
                                }
                            />
                        </Form.Item>
                        <Form.Item
                            name={`topicPattern_${info.id}`}
                            label="Topic Pattern"
                            rules={[{ required: false, message: 'Please input connection topic pattern allow!' }]}
                        >
                            <Input
                                placeholder="Please input connection topic pattern allow"
                                autoComplete="off"
                                defaultValue={info.topicPatternsAllow}
                                onChange={(e) =>
                                    updateDataSourceConnections(e.target.value, FormField.topicPatternsAllow, index)
                                }
                            />
                        </Form.Item>
                    </Space>
                </Card>
            );
        });
    };
    const getUserNamePassForm = (info: IFormConnectionData, index: number) => {
        return (
            <>
                <Form.Item
                    name={`username_${info.id}`}
                    label="User Name"
                    rules={[{ required: true, message: 'Please input connection userName!' }]}
                >
                    {/* username as value ,will input issue */}
                    <Input
                        placeholder="Please input connection username"
                        autoComplete="off"
                        onBlur={(e) => updateDataSourceConnections(e.target.value, FormField.username, index)}
                        defaultValue={info.username}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.username, index)}
                    />
                </Form.Item>
                <Form.Item
                    name={`password_${info.id}`}
                    label="Password"
                    rules={[{ required: true, message: 'Please input connection password!' }]}
                >
                    <Input.Password
                        placeholder="Please input connection password"
                        autoComplete="off"
                        defaultValue={info.password}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.password, index)}
                    />
                </Form.Item>
            </>
        );
    };

    const getHostPortForm = (info: IFormConnectionData, index: number) => {
        return (
            <>
                <Form.Item
                    name={`hostPort_${info.id}`}
                    label="Host port"
                    rules={[{ required: !isOracle(), message: 'Please input connection host port!' }]}
                >
                    <Input
                        placeholder="Please input connection host port"
                        disabled={isOracle() && info.tnsName !== '' && info.tnsName !== null && !formData.create}
                        autoComplete="off"
                        defaultValue={info.hostPort}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.hostPort, index)}
                    />
                </Form.Item>
            </>
        );
    };

    const getDatabaseForm = (info: IFormConnectionData, index: number) => {
        return (
            <>
                <Form.Item
                    name={`database_${info.id}`}
                    label="Database"
                    rules={[{ required: databaseRequired, message: 'Please input connection database!' }]}
                >
                    <Input
                        placeholder="Please input connection database"
                        autoComplete="off"
                        defaultValue={info.database}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.database, index)}
                    />
                </Form.Item>
            </>
        );
    };

    const getConnectionParamsForm = (info: IFormConnectionData, index: number) => {
        return (
            <>
                <Form.Item
                    name={`connectionParams_${info.id}`}
                    label="Connection Params"
                    rules={[{ required: false, message: 'Please input connection Params!' }]}
                >
                    <Input
                        placeholder="Please input connection Params"
                        autoComplete="off"
                        defaultValue={info.connectionParams}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.connectionParams, index)}
                    />
                </Form.Item>
            </>
        );
    };

    const getOracleTNSForm = (info: IFormConnectionData, index: number) => {
        return (
            <>
                <Form.Item
                    name={`tnsName_${info.id}`}
                    label="TNSName"
                    rules={[{ required: false, message: 'Please input connection tns name!' }]}
                >
                    <Input
                        placeholder="Please input connection TNS name"
                        autoComplete="off"
                        disabled={
                            isOracle() &&
                            (info.serviceName !== '' || info.hostPort !== '') &&
                            (info.serviceName !== null || info.hostPort !== null) &&
                            !formData.create
                        }
                        defaultValue={info.tnsName}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.tnsName, index)}
                    />
                </Form.Item>
            </>
        );
    };

    const getOracleServiceNameForm = (info: IFormConnectionData, index: number) => {
        return (
            <>
                {getHostPortForm(info, index)}
                <Form.Item
                    name={`serviceName_${info.id}`}
                    label="ServiceName"
                    rules={[{ required: false, message: 'Please input connection service name!' }]}
                >
                    <Input
                        placeholder="Please input connection service name"
                        disabled={isOracle() && info.tnsName !== '' && info.tnsName !== null && !formData.create}
                        autoComplete="off"
                        defaultValue={info.serviceName}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.serviceName, index)}
                    />
                </Form.Item>
            </>
        );
    };

    const getOracleForm = (info: IFormConnectionData, index: number) => {
        return (
            <>
                {getOracleTNSForm(info, index)}
                {getOracleServiceNameForm(info, index)}
            </>
        );
    };

    const getJDDBCParamsForm = (info: IFormConnectionData, index: number) => {
        return (
            <>
                <Form.Item
                    name={`jdbcParams_${info.id}`}
                    label="JDBC Params"
                    rules={[{ required: false, message: 'Please input JDBC Params!' }]}
                >
                    <Input
                        placeholder="Please input JDBC Params"
                        autoComplete="off"
                        defaultValue={info.jdbcParams}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.jdbcParams, index)}
                    />
                </Form.Item>
            </>
        );
    };

    const getConnectionPool = (info: IFormConnectionData, index: number) => {
        return (
            <>
                <Form.Item
                    name={`minSize_${info.id}`}
                    label="Min Size"
                    rules={[{ required: false, message: 'Please input connection min size!' }]}
                >
                    <Input
                        placeholder="Please input connection min size"
                        autoComplete="off"
                        defaultValue={info.minSize}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.minSize, index)}
                    />
                </Form.Item>
                <Form.Item
                    name={`maxSize_${info.id}`}
                    label="Max Size"
                    rules={[{ required: false, message: 'Please input connection max size!' }]}
                >
                    <Input
                        placeholder="Please input connection max size"
                        autoComplete="off"
                        defaultValue={info.maxSize}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.maxSize, index)}
                    />
                </Form.Item>
                <Form.Item
                    name={`idleSize_${info.id}`}
                    label="Idle Size"
                    rules={[{ required: false, message: 'Please input connection idle size!' }]}
                >
                    <Input
                        placeholder="Please input connection idle size"
                        autoComplete="off"
                        defaultValue={info.idleSize}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.idleSize, index)}
                    />
                </Form.Item>
                <Form.Item
                    name={`status_${info.id}`}
                    label="Status"
                    rules={[{ required: false, message: 'Please select status!' }]}
                >
                    <Switch
                        checkedChildren="1"
                        unCheckedChildren="0"
                        checked={info.status === '1'}
                        onChange={(value) => {
                            onChange(value, index);
                        }}
                    />
                </Form.Item>
            </>
        );
    };
    const getTrinoForm = (info: IFormConnectionData, index: number) => {
        return (
            <>
                <Form.Item
                    name={`catalog_${info.id}`}
                    label="Catalog"
                    rules={[{ required: false, message: 'Please input connection catalog!' }]}
                >
                    <Input
                        placeholder="Please input connection catalog"
                        autoComplete="off"
                        defaultValue={info.catalog}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.catalog, index)}
                    />
                </Form.Item>
                <Form.Item
                    name={`schema_${info.id}`}
                    label="Schema"
                    rules={[{ required: false, message: 'Please input connection schema!' }]}
                >
                    <Input
                        placeholder="Please input connection schema"
                        autoComplete="off"
                        defaultValue={info.schema}
                        onChange={(e) => updateDataSourceConnections(e.target.value, FormField.schema, index)}
                    />
                </Form.Item>
            </>
        );
    };
    const getJDBCConnections = (params: IFormConnectionData[]) => {
        return params.map((info: IFormConnectionData, index: number) => {
            return (
                <Card
                    style={{ marginTop: 16 }}
                    type="inner"
                    size="small"
                    title={getConnectionTitle(index)}
                    extra={index >= 1 && <DeleteOutlined onClick={() => removeConnectionItem(index)} />}
                    key={info.id}
                >
                    <Space direction="vertical" style={{ width: '100%', marginTop: 0 }}>
                        <Form.Item
                            name={`dataCenter_${info.id}`}
                            label="Data Center"
                            rules={[{ required: false, message: 'Please input connection data center!' }]}
                        >
                            <Select
                                defaultValue={info.dataCenter}
                                onChange={(value) => {
                                    dataCenterChangeHandler(value, FormField.dataCenter, index);
                                }}
                            >
                                {dataCenterOptions}
                            </Select>
                        </Form.Item>
                        {isOracle() && getOracleForm(info, index)}

                        {isTiDB() && getHostPortForm(info, index)}
                        {isPostgres() && getHostPortForm(info, index)}
                        {isPinot() && getHostPortForm(info, index)}
                        {isHive() && getHostPortForm(info, index)}
                        {isTrino() && getHostPortForm(info, index)}
                        {isPresto() && getHostPortForm(info, index)}
                        {isMysql() && getHostPortForm(info, index)}
                        {isSnowflake() && getHostPortForm(info, index)}

                        {getUserNamePassForm(info, index)}

                        {isSnowflake() && getConnectionParamsForm(info, index)}

                        {isPostgres() && getDatabaseForm(info, index)}
                        {isHive() && getDatabaseForm(info, index)}
                        {isTiDB() && getDatabaseForm(info, index)}
                        {isMysql() && getDatabaseForm(info, index)}
                        {isTrino() && getTrinoForm(info, index)}
                        {isPresto() && getTrinoForm(info, index)}
                        {isHive() && getJDDBCParamsForm(info, index)}
                        {isMysql() && getJDDBCParamsForm(info, index)}
                        {isPostgres() && getJDDBCParamsForm(info, index)}
                        {isTiDB() && getJDDBCParamsForm(info, index)}
                        {isTrino() && getJDDBCParamsForm(info, index)}
                        {isPresto() && getJDDBCParamsForm(info, index)}
                        {isHive() && getConnectionPool(info, index)}
                        {isMysql() && getConnectionPool(info, index)}
                        {isPostgres() && getConnectionPool(info, index)}
                        {isTiDB() && getConnectionPool(info, index)}
                        {isTrino() && getConnectionPool(info, index)}
                        {isPresto() && getConnectionPool(info, index)}
                        {isOracle() && getConnectionPool(info, index)}
                        {isPinot() && getConnectionPool(info, index)}
                        <Form.Item
                            name={`tablePattern_${info.id}`}
                            label="Table Pattern"
                            rules={[{ required: false, message: 'Please input connection table pattern allow!' }]}
                        >
                            <Input
                                placeholder="Please input connection table pattern allow"
                                autoComplete="off"
                                defaultValue={info.tablePatternAllow}
                                onChange={(e) =>
                                    updateDataSourceConnections(e.target.value, FormField.tablePatternAllow, index)
                                }
                            />
                        </Form.Item>
                        <Form.Item
                            name={`schemaPattern_${info.id}`}
                            label="Schema Pattern"
                            rules={[{ required: false, message: 'Please input connection schema pattern allow!' }]}
                        >
                            <Input
                                placeholder="Please input connection schema pattern allow"
                                autoComplete="off"
                                defaultValue={info.schemaPatternAllow}
                                onChange={(e) =>
                                    updateDataSourceConnections(e.target.value, FormField.schemaPatternAllow, index)
                                }
                            />
                        </Form.Item>
                        {enableSync && (
                            <Button
                                loading={testLoading}
                                onClick={() => onTestBtnClick(index)}
                                style={{ float: 'right' }}
                            >
                                Test Connection
                            </Button>
                        )}
                    </Space>
                </Card>
            );
        });
    };

    return (
        <>
            {loading && <Message type="loading" content="Loading..." style={messageStyle} />}
            {data && data.corpUser && (
                <Modal
                    title={title}
                    visible={visible}
                    onCancel={onClose}
                    width={900}
                    okText="Add"
                    style={{ paddingTop: 0 }}
                    footer={
                        <>
                            <Button
                                onClick={() => {
                                    onCancelBtnClick();
                                }}
                            >
                                Cancel
                            </Button>
                            <Button loading={saveLoading} onClick={onSaveBtnClick}>
                                Save
                            </Button>
                        </>
                    }
                >
                    <Space direction="vertical" style={{ width: '100%', marginTop: 0 }}>
                        <Form {...layout} form={form} name="control-ref">
                            {dataSourceBasic()}
                            <Card
                                style={{ marginTop: 16 }}
                                title="Connection Information"
                                extra={
                                    <>
                                        {formData.connections?.length < 2 && (
                                            <Button type="link" onClick={onAddMoreBtnClick}>
                                                Add GSB
                                            </Button>
                                        )}
                                    </>
                                }
                            >
                                {isInKafka() && getKafkaConnection(formData.connections)}
                                {isIceBerge() && getIceBergeConnection(formData.connections)}
                                {!isIceBerge() && !isInKafka() && getJDBCConnections(formData.connections)}
                            </Card>
                        </Form>
                    </Space>
                </Modal>
            )}
        </>
    );
}
