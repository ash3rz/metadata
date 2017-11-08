(ns metadata.services.favorites
  (:use [slingshot.slingshot :only [throw+]])
  (:require [metadata.persistence.favorites :as db]))

(defn- determine-filesystem-entity-types
  "Determines the set of entity types from the selected entity type in the request.

   Parameters:
     entity-type - the entity type from the request.

   Returns:
     The corresonding set of entity types."
  [entity-type]
  (if (= "any" entity-type)
    ["file" "folder"]
    [entity-type]))


(defn add-favorite
  "This function marks a given data item as a favorite of the authenticated user.

   Parameters:
     user - the user adding the favorite
     target-id - This is the `target-id` from the request.  It should be the UUID of the data item being
                marked
     target-type - the type of target (`analysis`|`app`|`file`|`folder`|`user`)."
  [user target-id target-type]
  (when-not (db/is-favorite? user target-id)
    (db/insert-favorite user target-id target-type))
  nil)


(defn remove-favorite
  "This function unmarks a given resource as a favorite of the authenticated user.

   Parameters:
     user - the user removing the favorite
     target-id - This is the `target-id` from the request.  It should be the UUID of the data item being
                 unmarked."
  [user target-id]
  (when-not (db/is-favorite? user target-id)
    (throw+ {:type      :clojure-commons.exception/not-found
             :user      user
             :target-id target-id
             :error     "The target-id wasn't marked as a favorite by the user"}))
  (db/delete-favorite user target-id)
  nil)


(defn list-favorite-data-ids
  "Returns a listing of a user's favorite data, including stat information about it. This endpoint
   is intended to help with paginating.

   Parameters:
     user - the user favorites to list
     entity-type - This is the value of the `entity-type` query parameter. It should be a string
                   containing one of the following: any|file|folder."
  [user entity-type]
  (let [entity-types (determine-filesystem-entity-types entity-type)]
    (->> (db/select-favorites-of-type user entity-types)
         (hash-map :filesystem))))


(defn remove-all-favorites
  "Removes all data resources marked as favorites for the authenticated user.

   Parameters:
     user - the user removing the favorites.
     entity-type - This is the value of the `entity-type` query parameter. It should be a string
                   containing one of the following: any|file|folder."
  [user entity-type]
  (db/delete-favorites-of-type user (determine-filesystem-entity-types entity-type)))


(defn filter-favorites
  "Given a list of UUIDs for data items, it filters the list, returning only the UUIDS that
   are marked as favorite by the authenticated user.

   Parameters:
     user - the user favorites to list
     body - This is the request body. It should be a map containing a :filesystem key with an array
            of UUIDs as its value."
  [user {uuids :filesystem}]
  (->> (db/select-favorites-of-type user ["file" "folder"] uuids)
       (hash-map :filesystem)))
