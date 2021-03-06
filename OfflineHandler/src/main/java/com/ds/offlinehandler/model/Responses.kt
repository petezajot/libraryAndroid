package com.ds.offlinehandler.model

import java.io.Serializable

class RequestResponse: Serializable{
    var id: Int          = 0
    var stageId: Int     = 0
    var name: String     = ""
    var json: String     = ""
    var endpoint: String = ""
    var folio: String    = ""
}

class ReqCollection: ArrayList<RequestResponse>(){}

class OfflineViable: Serializable{
    var countJson = 0
    var countUser = 0
    var countProducts = 0
}

class ViableCollection: ArrayList<OfflineViable>(){}