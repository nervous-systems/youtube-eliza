(defproject yt-eliza "0.1.0-SNAPSHOT"
  :description "Answering the hard questions w/ Clojurescript, AWS Lambda & Slack"
  :url "https://github.com/nervous-systems/youtube-eliza"
  :dependencies [[org.clojure/clojure            "1.8.0"]
                 [org.clojure/clojurescript      "1.8.34"]
                 [org.clojure/core.async         "0.2.374"]

                 [io.nervous/cljs-lambda         "0.3.0"]
                 [io.nervous/cljs-nodejs-externs "0.2.0"]
                 [io.nervous/eulalie             "0.6.4"]
                 [io.nervous/kvlt                "0.1.1"]]
  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-npm       "0.6.0"]
            [lein-doo       "0.1.7-SNAPSHOT"]
            [io.nervous/lein-cljs-lambda "0.5.1"]]
  :npm {:dependencies [[entities           "1.1.1"]
                       [source-map-support "0.4.0"]]}
  :resource-paths ["static"]
  :source-paths   ["src"]
  :cljs-lambda
  {:defaults      {:role "FIXME"}
   :resource-dirs ["static"]
   :functions
   [{:name        "yt-eliza"
     :invoke      yt-eliza.handlers/yt-eliza
     :timeout     20}
    {:name        "yt-eliza-gateway"
     :invoke      yt-eliza.handlers/yt-eliza-gateway
     :memory-size 512}]}
  :cljsbuild
  {:builds [{:id "yt-eliza"
             :source-paths ["src"]
             :compiler {:output-to     "target/yt-eliza/yt_eliza.js"
                        :output-dir    "target/yt-eliza"
                        :source-map    "target/yt-eliza/yt_eliza.js.map"
                        :target        :nodejs
                        :language-in   :ecmascript5
                        :optimizations :advanced}}]})
