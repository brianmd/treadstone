(ns treadstone.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [treadstone.core-test]))

(doo-tests 'treadstone.core-test)

