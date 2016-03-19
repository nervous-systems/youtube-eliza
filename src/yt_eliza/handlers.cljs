(ns yt-eliza.handlers
  (:require [eulalie.creds]
            [eulalie.lambda.util :as lambda]
            [yt-eliza.core       :as core]
            [yt-eliza.youtube    :as yt]
            [yt-eliza.util       :as util]
            [cljs.nodejs         :as nodejs]
            [kvlt.core           :as kvlt]
            [promesa.core :as p  :refer-macros [alet]]
            [cljs-lambda.macros  :refer-macros [deflambda]]
            [cljs.core.async     :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def config      (util/read-config))
(def youtube-key (config :youtube-key))

(def entities (nodejs/require "entities"))

(defn- tidy-attachment [attachment]
  (update attachment :text #(.decodeHTML entities %)))

(defn- tidy-response [{:keys [attachments] :as resp}]
  (cond-> resp
    attachments (assoc :attachments (map tidy-attachment attachments))))

(deflambda yt-eliza "Asynchronously invoked, handles command responses"
  [{:keys [query url]} ctx]
  (alet [videos   (p/await (yt/video-search!   youtube-key query))
         comment  (p/await (core/find-comment! youtube-key videos))
         body     (if comment
                    (core/comment->channel-response query comment)
                    (config :not-found-response))]
    (kvlt/request!
     {:method :post
      :url    url
      :type   :json
      :form   (tidy-response body)})))

(deflambda yt-eliza-gateway "Slack command entrypoint"
  [{:keys [token text response_url] :as input} ctx]
  (when (not= token (config :slack-token))
    (throw (ex-info "Unauthorized" {:type :bad-token})))
  (let [event {:query text :url response_url}]
    (go
      (<! (lambda/invoke! (eulalie.creds/env) "yt-eliza" :event event))
      (config :processing-response))))
