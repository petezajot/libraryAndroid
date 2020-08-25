package com.development.offlinehandler.model

import com.google.gson.annotations.SerializedName


data class ApiRequestGetData(@SerializedName("Body") val Body: ArrayList<BodyContent>,
                             @SerializedName("TransactionId") val TransactionId: String,
                             @SerializedName("IsOK") val IsOK: Boolean,
                             @SerializedName("Messages") val Messages: String)

data class BodyContent(@SerializedName("Id") val Id: Int,
                       @SerializedName("Name") val Name: String,
                       @SerializedName("Description") val Description: String,
                       @SerializedName("Version") val Version: Int,
                       @SerializedName("Stages") val Stages: ArrayList<StagesContent>)

data class StagesContent(@SerializedName("Id") val Id: Int,
                         @SerializedName("Name") val Name: String,
                         @SerializedName("Description") val Description: String,
                         @SerializedName("Sequence") val Sequence: Int,
                         @SerializedName("FlowState") val FlowState: String,
                         @SerializedName("StageGroup") val StageGroup: String,
                         @SerializedName("CurrentStageGroup") val CurrentStageGroup: Int,
                         @SerializedName("TotalStageGroups") val TotalStageGroups: Int,
                         @SerializedName("Actor") val Actor: ActorContent,
                         @SerializedName("QuestionGroups") val QuestionGroups: ArrayList<Any>,
                         @SerializedName("Properties") val Properties: ArrayList<PropertiesContent>,
                         @SerializedName("Roles") val Roles: ArrayList<Any>,
                         @SerializedName("Products") val Products: ArrayList<ProductsContent>,//Products
                         @SerializedName("Icon") val Icon: IconContent,//Icon
                         @SerializedName("File") val File: Int)

data class PropertiesContent(@SerializedName("Name") val Name: String,
                             @SerializedName("Value") val Value: String)

data class ProductsContent(@SerializedName("Id") val Id: Int,
                           @SerializedName("Name") val Name: String,
                           @SerializedName("Description") val Description: String,
                           @SerializedName("QuestionGroups") val QuestionGroups: Int,
                           @SerializedName("Actors") val Actors: ActorContent,
                           @SerializedName("WorkFlows") val WorkFlows: ArrayList<WFContent>,
                           @SerializedName("Icon") val Icon: IconContent
)//Icon

data class IconContent(@SerializedName("Name") val Name: String, @SerializedName("Identifier") val Identifier: String)

data class WFContent(@SerializedName("Id") val Id: Int,
                     @SerializedName("Name") val Name: String,
                     @SerializedName("Description") val Description: String,
                     @SerializedName("Version") val Version: Int,
                     @SerializedName("Stages") val Stages: ArrayList<StagesContent>,
                     @SerializedName("Colors") val Colors: ArrayList<Any>,//Colors
                     @SerializedName("StageGroups") val StageGroups: ArrayList<Any>,//StageGroups
                     @SerializedName("Properties") val Properties: ArrayList<PropertiesContent>,
                     @SerializedName("Icon") val Icon: IconContent
)

data class ActorContent(@SerializedName("Id") val Id: Int,
                        @SerializedName("Name") val Name: String,
                        @SerializedName("Description") val Description: String,
                        @SerializedName("Sequence") val Sequence: Int,
                        @SerializedName("Required") val Required: Boolean,
                        @SerializedName("DocumentalGroup") val DocumentalGroup: ArrayList<Any>,
                        @SerializedName("Icon") val Icon: IconContent
)

//Request
data class RequestContent(@SerializedName("Body") val Body: String,
                          @SerializedName("EncryptedBody") val EncryptedBody: String,
                          @SerializedName("SecurityData") val SecurityData: String,
                          @SerializedName("PKey") val PKey: String
)
//Modelo para el usuario (offline stages)
data class OfflineStageData(@SerializedName("stageId") val stageId: Int,
                            @SerializedName("name") val name: String,
                            @SerializedName("json") val json: String,
                            @SerializedName("endpoint") val endpoint: String,
                            @SerializedName("seq") val seq: Int){
    constructor():this(0, "", "", "", 0)
}
//Modelo para salvar el usuario
data class OfflineUserData(@SerializedName("userId") val userId: Int,
                           @SerializedName("userName") val userName: String,
                           @SerializedName("name") val name: String,
                           @SerializedName("apat") val apat: String,
                           @SerializedName("amat") val amat: String,
                           @SerializedName("email") val email: String,
                           @SerializedName("phone") val phone: String,
                           @SerializedName("token") val token: String,
                           @SerializedName("pass") val pass: String){
    constructor():this(0, "", "", "", "", "", "", "", "")
}