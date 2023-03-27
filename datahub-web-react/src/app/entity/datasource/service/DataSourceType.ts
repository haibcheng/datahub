export interface IFormConnectionData {
    id?: number;
    bootstrap?: string;
    schemaRegistryUrl?: string;
    topicPatternsAllow?: string;
    topicPatternsDeny?: string;
    topicPatternsIgnoreCase?: string;
    connectionParams?: string;
    username?: string;
    password?: string;
    hostPort?: string;
    database?: string;
    tnsName?: string;
    serviceName?: string;
    catalog?: string;
    schema?: string;
    jdbcParams?: string;
    minSize: number;
    maxSize: number;
    idleSize: number;
    status: string;
    dataCenter?: string;
    databaseAlias?: string;
    tablePatternAllow?: string;
    tablePatternDeny?: string;
    tablePatternIgnoreCase?: string;
    schemaPatternAllow?: string;
    schemaPatternDeny?: string;
    schemaPatternIgnoreCase?: string;
    viewPatternAllow?: string;
    viewPatternDeny?: string;
    viewPatternIgnoreCase?: string;
    includeTables?: string;
    includeViews?: string;
    hiveMetastoreUris?: string;
}

export interface IFormData {
    sourceType: string;
    name: string;
    alias: string;
    testQuerySql: string;
    create: boolean;
    group: string;
    region: string;
    connections: any[];
}

export enum FormField {
    bootstrap = 'bootstrap',
    schemaRegistryUrl = 'schemaRegistryUrl',
    database = 'database',
    tnsName = 'tnsName',
    jdbcParams = 'jdbcParams',
    minSize = 'minSize',
    maxSize = 'maxSize',
    idleSize = 'idleSize',
    status = 'status',
    alias = 'alias',
    owned = 'owned',
    testQuerySql = 'testQuerySql',
    dataCenter = 'dataCenter',
    connectionParams = 'connectionParams',
    driver = 'driver',
    group = 'group',
    hiveMetastoreUris = 'hiveMetastoreUris',
    hostPort = 'hostPort',
    name = 'name',
    password = 'password',
    region = 'region',
    schemaPatternAllow = 'schemaPatternAllow',
    sourceType = 'sourceType',
    tablePatternAllow = 'tablePatternAllow',
    topicPatternsAllow = 'topicPatternsAllow',
    topicSplitField = 'topicSplitField',
    username = 'username',
    serviceName = 'serviceName',
    catalog = 'catalog',
    schema = 'schema',
}

export enum NotificationLevel {
    SUCCESS = 'success',
    INFO = 'info',
    WARNING = 'warning',
    ERROR = 'error',
}

export interface IIcebergSourceInput {
    hiveMetastoreUris: string;
}
export interface IKafkaMetadataSourceInput {
    bootstrap: string;
    schemaRegistryUrl: string;
    topicPatternsAllow?: string;
    topicPatternsDeny?: string;
    topicPatternsIgnoreCase?: boolean;
}
export interface IBasicDataSourceInput {
    username: string;
    password: string;
    databaseAlias?: string;
    tablePatternAllow?: string;
    tablePatternDeny?: string;
    tablePatternIgnoreCase?: boolean;
    schemaPatternAllow?: string;
    schemaPatternDeny?: string;
    schemaPatternIgnoreCase?: boolean;
    viewPatternAllow?: string;
    viewPatternDeny?: string;
    viewPatternIgnoreCase?: boolean;
    includeTables?: boolean;
    includeViews?: boolean;
}
export interface IPinotSourceInput extends IBasicDataSourceInput {
    hostPort: string;
    minSize?: number;
    maxSize?: number;
    idleSize?: number;
    status?: string;
}
export interface IPrestoSourceInput extends IBasicDataSourceInput {
    hostPort: string;
    catalog?: string;
    schema?: string;
    jdbcParams?: string;
    minSize?: number;
    maxSize?: number;
    idleSize?: number;
    status?: string;
}
export interface ITrinoSourceInput extends IBasicDataSourceInput {
    hostPort: string;
    catalog?: string;
    schema?: string;
    jdbcParams?: string;
    minSize?: number;
    maxSize?: number;
    idleSize?: number;
    status?: string;
}
export interface IHiveSourceInput extends IBasicDataSourceInput {
    hostPort: string;
    database: string;
    jdbcParams?: string;
    minSize?: number;
    maxSize?: number;
    idleSize?: number;
    status?: string;
}
export interface IMysqlSourceInput extends IBasicDataSourceInput {
    hostPort: string;
    database: string;
    jdbcParams?: string;
    minSize?: number;
    maxSize?: number;
    idleSize?: number;
    status?: string;
}
export interface IPostgresSourceInput extends IBasicDataSourceInput {
    hostPort: string;
    database: string;
    jdbcParams?: string;
    minSize?: number;
    maxSize?: number;
    idleSize?: number;
    status?: string;
}
export interface ITiDBSourceInput extends IBasicDataSourceInput {
    hostPort: string;
    database: string;
    jdbcParams?: string;
    minSize?: number;
    maxSize?: number;
    idleSize?: number;
    status?: string;
}
export interface IOracleSourceInput extends IBasicDataSourceInput {
    hostPort?: string;
    tnsName?: string;
    serviceName?: string;
    minSize?: number;
    maxSize?: number;
    idleSize?: number;
    status?: string;
}

export interface ISnowflakeSourceInput extends IBasicDataSourceInput {
    hostPort?: string;
    minSize?: number;
    maxSize?: number;
    idleSize?: number;
    status?: string;
    connectionParams?: string;
}

export interface IDatasourceSourceInput {
    dataCenter: string;
    iceberg?: IIcebergSourceInput;
    kafka?: IKafkaMetadataSourceInput;
    mysql?: IMysqlSourceInput;
    oracle?: IOracleSourceInput;
    postgres?: IPostgresSourceInput;
    tiDB?: ITiDBSourceInput;
    hive?: IHiveSourceInput;
    presto?: IPrestoSourceInput;
    trino?: ITrinoSourceInput;
    pinot?: IPinotSourceInput;
    snowflake?: ISnowflakeSourceInput;
}
