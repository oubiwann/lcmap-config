(ns lcmap.config.components
  "LCMAP REST configuration components

  Large applications often consist of many stateful processes which must be
  started and stopped in a particular order. The component model makes
  those relationships explicit and declarative, instead of implicit in
  imperative code. The LCMAP REST service project is one such application
  and early on in its existence it was refactored to support the
  component/dependency-injection approach.

  The approach taken in the LCMAP REST application is the following:

   * The primary entry point starts the top-level system map (this is
     done in lcmap.rest.app.main).
   * The top-level system map is defined in lcmap.rest.components.init --
     this is what is started in the main function.
   * init takes any parameters not defined in configuration (e.g., the
     top-level Ring handler) and instantiates each component. At the same
     time it defines each component's dependencies.
   * During startup the components are brought up in dependency order with
     each one's start function getting called.
   * During shutdown this order is revered and the shutdown function for
     each component is called.

  For components whose state (e.g. configuration values or changes to state
  data that get made during startup) is needed in other parts of the code
  (e.g. web code needing access to db connections), these dependencies need
  to be appropriately injected. In order for other parts of the codebase to
  be made aware of a particular component, changes will have to be made to
  those parts. (For an example of this, see how the Ring handlers are
  updated by the component to ensure that routes have access to the database
  and event handler.)

  For more information on the Clojure component library, see:

   * https://github.com/stuartsierra/component
   * https://www.youtube.com/watch?v=13cmHf_kt-Q"
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [lcmap.config]
            [lcmap.config.components.config :as config]
            [lcmap.config.components.system :as system]))

(defn init []
  (component/system-map
    :cfg (config/new-configuration lcmap.config/defaults)
    :sys (component/using
           (system/new-lcmap-toplevel)
           [:cfg])))

(defn stop [system component-key]
  (->> system
       (component-key)
       (component/stop)
       (assoc system component-key)))

(defn start [system component-key]
  (->> system
       (component-key)
       (component/start)
       (assoc system component-key)))

(defn restart [system component-key]
  (-> system
      (stop component-key)
      (start component-key)))
