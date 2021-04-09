package br.ufc.geonotas.utils

class Constants {
    companion object{

        const val TABLE_USERS = "Users"
        const val TABLE_NOTES = "Notes"
        const val TABLE_COMMENTS = "Comments"

        const val DIR_AVATARS = "avatars/"

        //Tests
       const val TEST : Boolean = true
       const val USER_NICK_SECTION = "Nimguem2k"


        const val MAX_LOCATION: Int = 1
        const val REQUEST_CODE_MAP: Int = 2
        const val REQUEST_CODE_OPEN_NOTE: Int = 3

        const val ACTION_FULL_MAP = 4
        const val ACTION_POINT_MAP = 5
        const val REQUEST_CODE_NEW_NOTE = 6
        const val REQUEST_CODE_SELECT_AVATAR = 7
        const val REQUEST_CODE_PROFILE = 8
    }
}