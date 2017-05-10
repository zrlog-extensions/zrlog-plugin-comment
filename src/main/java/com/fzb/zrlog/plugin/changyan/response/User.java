package com.fzb.zrlog.plugin.changyan.response;

public class User {


    /**
     * user_id : 378333
     * name : 欢喜佛祖
     * url : http://t.qq.com/hsu5199
     * avatar_url : http://app.qlogo.cn/mbloghead/d4aab6bf54582d4f7a18/50
     * threads : 0
     * comments : 0
     * social_uid : {"qq":"83B14496C07AFD09ABA30EE8BA7A284C"}
     * post_votes : 0
     * connected_services : {"qqt":{"name":"欢喜佛祖","email":"602532062@qq.com","avatar_url":"http://app.qlogo.cn/mbloghead/d4aab6bf54582d4f7a18/50","url":"http://t.qq.com/hsu5199","description":"我是台湾台北人現定居大連，所有了解我認識我的朋友們都叫我(佛祖)，所以您叫我(佛祖)这名稱就好，我个人處世之道嗎！就是：人與人相處之道,誠信相交,以誠相待,信義為本.....","service_name":"qqt"},"qzone":{"name":"欢喜佛祖","avatar_url":null,"service_name":"qzone"}}
     */

    private ResponseBean response;
    /**
     * response : {"user_id":"378333","name":"欢喜佛祖","url":"http://t.qq.com/hsu5199","avatar_url":"http://app.qlogo.cn/mbloghead/d4aab6bf54582d4f7a18/50","threads":0,"comments":0,"social_uid":{"qq":"83B14496C07AFD09ABA30EE8BA7A284C"},"post_votes":"0","connected_services":{"qqt":{"name":"欢喜佛祖","email":"602532062@qq.com","avatar_url":"http://app.qlogo.cn/mbloghead/d4aab6bf54582d4f7a18/50","url":"http://t.qq.com/hsu5199","description":"我是台湾台北人現定居大連，所有了解我認識我的朋友們都叫我(佛祖)，所以您叫我(佛祖)这名稱就好，我个人處世之道嗎！就是：人與人相處之道,誠信相交,以誠相待,信義為本.....","service_name":"qqt"},"qzone":{"name":"欢喜佛祖","avatar_url":null,"service_name":"qzone"}}}
     * code : 0
     */

    private int code;

    public ResponseBean getResponse() {
        return response;
    }

    public void setResponse(ResponseBean response) {
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static class ResponseBean {
        private String user_id;
        private String name;
        private String url;
        private String avatar_url;
        private int threads;
        private int comments;
        /**
         * qq : 83B14496C07AFD09ABA30EE8BA7A284C
         */

        private SocialUidBean social_uid;
        private String post_votes;
        /**
         * qqt : {"name":"欢喜佛祖","email":"602532062@qq.com","avatar_url":"http://app.qlogo.cn/mbloghead/d4aab6bf54582d4f7a18/50","url":"http://t.qq.com/hsu5199","description":"我是台湾台北人現定居大連，所有了解我認識我的朋友們都叫我(佛祖)，所以您叫我(佛祖)这名稱就好，我个人處世之道嗎！就是：人與人相處之道,誠信相交,以誠相待,信義為本.....","service_name":"qqt"}
         * qzone : {"name":"欢喜佛祖","avatar_url":null,"service_name":"qzone"}
         */

        private ConnectedServicesBean connected_services;

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
            this.avatar_url = avatar_url;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public int getComments() {
            return comments;
        }

        public void setComments(int comments) {
            this.comments = comments;
        }

        public SocialUidBean getSocial_uid() {
            return social_uid;
        }

        public void setSocial_uid(SocialUidBean social_uid) {
            this.social_uid = social_uid;
        }

        public String getPost_votes() {
            return post_votes;
        }

        public void setPost_votes(String post_votes) {
            this.post_votes = post_votes;
        }

        public ConnectedServicesBean getConnected_services() {
            return connected_services;
        }

        public void setConnected_services(ConnectedServicesBean connected_services) {
            this.connected_services = connected_services;
        }

        public static class SocialUidBean {
            private String qq;

            public String getQq() {
                return qq;
            }

            public void setQq(String qq) {
                this.qq = qq;
            }
        }

        public static class ConnectedServicesBean {
            /**
             * name : 欢喜佛祖
             * email : 602532062@qq.com
             * avatar_url : http://app.qlogo.cn/mbloghead/d4aab6bf54582d4f7a18/50
             * url : http://t.qq.com/hsu5199
             * description : 我是台湾台北人現定居大連，所有了解我認識我的朋友們都叫我(佛祖)，所以您叫我(佛祖)这名稱就好，我个人處世之道嗎！就是：人與人相處之道,誠信相交,以誠相待,信義為本.....
             * service_name : qqt
             */

            private QqtBean qqt;
            /**
             * name : 欢喜佛祖
             * avatar_url : null
             * service_name : qzone
             */

            private QzoneBean qzone;

            public QqtBean getQqt() {
                return qqt;
            }

            public void setQqt(QqtBean qqt) {
                this.qqt = qqt;
            }

            public QzoneBean getQzone() {
                return qzone;
            }

            public void setQzone(QzoneBean qzone) {
                this.qzone = qzone;
            }

            public static class QqtBean {
                private String name;
                private String email;
                private String avatar_url;
                private String url;
                private String description;
                private String service_name;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getEmail() {
                    return email;
                }

                public void setEmail(String email) {
                    this.email = email;
                }

                public String getAvatar_url() {
                    return avatar_url;
                }

                public void setAvatar_url(String avatar_url) {
                    this.avatar_url = avatar_url;
                }

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public String getDescription() {
                    return description;
                }

                public void setDescription(String description) {
                    this.description = description;
                }

                public String getService_name() {
                    return service_name;
                }

                public void setService_name(String service_name) {
                    this.service_name = service_name;
                }
            }

            public static class QzoneBean {
                private String name;
                private Object avatar_url;
                private String service_name;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public Object getAvatar_url() {
                    return avatar_url;
                }

                public void setAvatar_url(Object avatar_url) {
                    this.avatar_url = avatar_url;
                }

                public String getService_name() {
                    return service_name;
                }

                public void setService_name(String service_name) {
                    this.service_name = service_name;
                }
            }
        }
    }
}
