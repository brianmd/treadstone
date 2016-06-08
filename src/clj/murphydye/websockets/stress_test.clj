(println "loading murphydye.websockets.stress-test")

(ns murphydye.websockets.stress-test
  (:require [clojure.tools.logging :as log]
            [immutant.web.async :as async]
            [cognitect.transit :as transit]
            [clj-time.core :as t]
            [clojure.set :refer [difference]]

            [murphydye.utils.core :refer :all]
            [murphydye.websockets.router :as r :refer [add]]
            [murphydye.websockets.core :as ws]
            ))

"
from client:
   :start-test (keys){:client-id :num-threads :num-iterations :wait?}
   :received (keys){:client-id :seq-num}

from server:
   :iteration (keys){:client-id :seq-num}
   :finished {:client-id client-id
              :status [:ok|:failed]
              :received-notifications [...]
              :missing [...]}
"

;; (def test-seq-num (atom 0))
;; (do
;;   (swap! client-has-received assoc 1 [])
;;   (map! (partial received-count 1) (range 5 10))
;;   (println @client-has-received))
;; (send-msg 3 [:iteration 5 4 3])
;; (stress-test-thread 1 3 (range 2 7))
;; (send-received 1 nil)
;; (let [n (atom 0)]
;;   (stress-test 478 1 3 7)
;;   ;; (println @n)
;;   )

(def client-has-received (atom {}))

(defn received-count [client-id seq-num]
  (swap! client-has-received update client-id (fn [v] (conj v seq-num))))

(defn send-msg [conn msg]
  (future
    (let [v (vec (conj (seq msg) :stress-test))]
      (ws/send-to-connection conn v)
      ;; (ws/send-to-connection conn (vector (seq msg :stress-test)))
      ;; (println v)
      )))

(defn stress-test-thread [client-id conn v]
  ;; (log/info "client-id" client-id (type conn) v)
  (doseq [n v]
    (log/info "n:" n)
    (send-msg conn [:iteration {:client-id client-id :seq-num n}])))

(defn wait-for-client-to-finish [client-id start-time previous]
  (Thread/sleep 100)
  (let [current (@client-has-received client-id)]
    (if (= current previous)
      (/ (t/in-millis (t/interval start-time (t/now))) 1000.0)
      (recur client-id start-time current))))

(defn send-received [client-id conn num-iterations]
  (let [latency (wait-for-client-to-finish client-id (t/now) (@client-has-received client-id))
        expected (range num-iterations)
        received (@client-has-received client-id)
        missing (vec (sort (difference (set expected) (set received))))
        status (if (empty? missing) :ok :failed)]
    (log/info [:finished {:client-id client-id :status status :received-notifications received :missing missing :latency latency}])
    (send-msg conn [:finished {:client-id client-id :status status :received-notifications received :missing missing :latency latency}])
    latency))

(defn stress-test [client-id conn num-threads num-iterations]
  (log/info "in stress-test")
  (log/info client-id)
  (log/info num-threads)
  (log/info num-iterations)
  (log/info (range num-iterations))
  (try
    (swap! client-has-received assoc client-id [])
    (let [partition-size (Math/ceil (/ num-iterations num-threads))
          partitions (partition-all partition-size (range num-iterations))
          futures (map! #(future (stress-test-thread client-id conn %))
                        partitions)
          ]
      ;; (stress-test-thread client-id conn [3 4 5])
      (map! deref futures)  ;; wait until all are done
      (ppa partitions)
      (log/info (now))
      (future
        (Thread/sleep 100)
        (send-received client-id conn num-iterations)
        (log/info (now))
        (log/info "got to here"))
      )
    (finally
      (swap! client-has-received dissoc client-id)
      (println "done"))))

(defn start-test-action-aux [state _ m]
  (log/info "in start-test-action-aux")
  (stress-test (:client-id m) ws/*connection* (:num-threads m) (:num-iterations m)))

(defn start-test-action [state action-name m]
  (log/info "in start-test-action")
  (log/info state)
  (log/info action-name)
  (start-test-action-aux state action-name m))

(defn received-action [state _ m]
  (received-count (:client-id m) (:seq-num m)))

(defn create-actor [actor-name]
  (let [actor (r/make-router actor-name (atom {}))]
    (add actor :start-test start-test-action)
    (add actor :received received-action)
    actor
    ))

;; (defonce stress-test-actor (r/make-actor (atom {})))

;; (add stress-test-actor :start-test start-test-action)
;; (add stress-test-actor :received received-action)

