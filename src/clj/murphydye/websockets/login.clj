;; Will add login info to connection
;; :customer and :authentication-token (to enable opening summit.com)

(ns murphydye.websockets.login
  (:require [cheshire.core :as ches]
            [clj-http.client :as client]
            ;; [clojure.tools.logging :as log]
            ;; [immutant.web.async :as async]
            ;; [cognitect.transit :as transit]

            [murphydye.utils.core :refer [clojurize-map pp examples]]
            [murphydye.websockets.router :as r :refer [add dispatch]]
            [murphydye.websockets.core :as ws]
            ))

(defn login [state _ m])
(defn logout [state _ m])
;; (defn authenticity-token [state [_]]
;;   (:authenticity-token *websocket-connection*))

(defn bh-login [email pw]
  (let [cred
        {"customer"
         {"email" email, "password" pw}
         "session"
         {"customer"
          {"email" email, "password" pw}
          }}
        params
        {:body         (ches/generate-string cred)
         :content-type :json
         :accept       :json}
        result (client/post
                "https://www.summit.com/store/customers/sign_in.json"
                params)
        ;; (clojurize-map-keywords
        result (assoc result :body (ches/parse-string (:body result)))
        m (clojurize-map (clojure.walk/keywordize-keys result))]
    (assoc m
           :auth-token (:X-CSRF-Token (:headers m))
           :customer (-> m :body :customers first)
           )))

(examples
 (def username "bilbo@example.com")
 (def password "secretdecoderring")
 (bh-login username password)
 (def login-info (bh-login username password))
 (pp login-info)
 )
