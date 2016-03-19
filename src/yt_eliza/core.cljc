(ns yt-eliza.core
  (:require [clojure.string :as str]
            [promesa.core #?(:clj :refer :cljs :refer-macros) [alet]]
            [yt-eliza.youtube :as yt]
            [yt-eliza.util :as util]))

(defn- score-comment [{:keys [text]}]
  (count text))

(defn find-comment! [api-key [video & videos]]
  (when video
    (alet [comments (p/await (yt/video-comments! api-key video))
           filtered (for [c comments :when (not (util/contains-html? (c :text)))]
                      c)]
      (or (util/weighted-choice (zipmap filtered (map score-comment filtered)))
          (find-comment! api-key videos)))))

(defn comment->channel-response [query {:keys [author text]}]
  {:text          query
   :response_type "in_channel"
   :attachments   [{:fallback text
                    :text     text
                    :author_name (author :name)
                    :author_icon (util/unquery-url (author :avatar))}]})
