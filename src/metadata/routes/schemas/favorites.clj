(ns metadata.routes.schemas.favorites
  (:use [common-swagger-api.schema :only [describe StandardUserQueryParams]]
        [metadata.routes.schemas.common])
  (:require [schema.core :as s]))

(s/defschema FavoritesDataListingParams
  (assoc StandardUserQueryParams
    :entity-type (describe (s/enum "any" "file" "folder") "The type of the requested data items.")))
