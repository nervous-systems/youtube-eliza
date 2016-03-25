(ns yt-eliza.youtube
  (:require [clojure.string :as str]
            [promesa.core :as p #?(:clj :refer :cljs :refer-macros) [alet]]
            [kvlt.core :as kvlt]))

(def youtube-url-prefix "https://www.googleapis.com/youtube/v3")

(defn- youtube-url [url-parts]
  (str/join "/" (into [youtube-url-prefix] (map name url-parts))))

(defn youtube! [api-key url-parts query & [{:keys [limit] :or {limit 10}}]]
  (p/then
    (kvlt/request! {:url   (youtube-url url-parts)
                    :as    :json
                    :query (merge
                            {:maxResults limit
                             :part       "snippet"
                             :key        api-key}
                            query)})
    :body))

(defn video-search! [api-key search-term & [{:keys [limit] :or {limit 5}}]]
  (alet [{items :items} (p/await (youtube! api-key [:search]
                                           {:q search-term :type "video"}))]
    (for [{:keys [id]} items]
      (id :videoId))))

(defn ->comment [m]
  (when-let [snippet (some-> m :snippet :topLevelComment :snippet)]
    {:text   (snippet :textDisplay)
     :author {:name   (snippet :authorDisplayName)
              :avatar (snippet :authorProfileImageUrl)}}))

(defn video-comments! [api-key video-id & [{:keys [limit] :or {limit 5}}]]
  (alet [{items :items} (p/await (youtube! api-key [:commentThreads] {:videoId video-id}))]
    (keep ->comment items)))
