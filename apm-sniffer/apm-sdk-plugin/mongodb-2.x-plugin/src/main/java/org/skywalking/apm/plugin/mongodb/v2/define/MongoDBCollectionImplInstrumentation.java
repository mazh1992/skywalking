/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.plugin.mongodb.v2.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
import static org.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * {@link MongoDBCollectionImplInstrumentation} define that the MongoDB Java Driver 2.13.x-2.14.x plugin intercepts the
 * following methods in the {@link com.mongodb.DBCollectionImpl}class:
 * 1. find <br/>
 * 2. insert <br/>
 * 3. insertImpl <br/>
 * 4. update <br/>
 * 5. updateImpl <br/>
 * 6. remove <br/>
 * 7. createIndex <br/>
 *
 * @author liyuntao
 */
public class MongoDBCollectionImplInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "com.mongodb.DBCollectionImpl";

    private static final String MONGDB_METHOD_INTERCET_CLASS = "org.skywalking.apm.plugin.mongodb.v2.MongoDBCollectionMethodInterceptor";

    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[] {
            new ConstructorInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getConstructorMatcher() {
                    return any();
                }

                @Override
                public String getConstructorInterceptor() {
                    return MONGDB_METHOD_INTERCET_CLASS;
                }
            }
        };
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {

            new InterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("find").and(takesArguments(9));
                }

            },
            new InterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("insert").and(takesArguments(4));
                }

            },
            new InterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("insertImpl");
                }

            },
            new InterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("update");
                }
            },
            new InterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("updateImpl");
                }

            },
            new InterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("remove").and(takesArguments(4));
                }

            },
            new InterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("createIndex");
                }

            },

        };
    }

    @Override
    protected ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }

    @Override
    protected String[] witnessClasses() {
        /**
         * @see {@link com.mongodb.tools.ConnectionPoolStat}
         */
        return new String[] {
            "com.mongodb.tools.ConnectionPoolStat"
        };
    }

}
