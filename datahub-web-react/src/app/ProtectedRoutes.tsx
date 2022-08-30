import React from 'react';
import { Switch, Route } from 'react-router-dom';
import { Layout } from 'antd';
import { HomePage } from './home/HomePage';
import AppConfigProvider from '../AppConfigProvider';
import { SearchRoutes } from './SearchRoutes';
import { PageRoutes } from '../conf/Global';

/**
 * Container for all views behind an authentication wall.
 */
export const ProtectedRoutes = (): JSX.Element => {
    return (
        <AppConfigProvider>
            <Layout style={{ height: '100%', width: '100%' }}>
                <Layout>
                    <Switch>
                        <Route exact path={PageRoutes.HOME} render={() => <HomePage />} />
                        <Route path="/*" render={() => <SearchRoutes />} />
                    </Switch>
                </Layout>
            </Layout>
        </AppConfigProvider>
    );
};
