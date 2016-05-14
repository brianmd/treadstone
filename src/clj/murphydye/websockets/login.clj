;; Will add login info to connection
;; :customer and :authentication-token (to enable opening summit.com)

(ns murphydye.websockets.login
  (:require ;
            ;; [clojure.tools.logging :as log]
            ;; [immutant.web.async :as async]
            ;; [cognitect.transit :as transit]

            [murphydye.websockets.router :as r :refer [add dispatch]]
            [murphydye.websockets.core :as ws]
            ))

(defn login [state _ m])
(defn logout [state _ m])
;; (defn authenticity-token [state [_]]
;;   (:authenticity-token *websocket-connection*))

