package com.example.smartcommunicationandnoticemanagement.navigation

sealed class NavRoute(val route: String) {
    object Splash : NavRoute("splash")
    object Login : NavRoute("login")
    object Register : NavRoute("register")
    object ForgotPassword : NavRoute("forgot_password")
    
    object StudentDashboard : NavRoute("student_dashboard")
    object AdminDashboard : NavRoute("admin_dashboard")
    
    object EditProfile : NavRoute("edit_profile")
    object FAQ : NavRoute("faq")
    
    object NoticeDetail : NavRoute("notice_detail/{noticeId}") {
        fun withId(id: String) = "notice_detail/$id"
    }

    object GroupChat : NavRoute("group_chat/{semester}") {
        fun withSemester(s: Int) = "group_chat/$s"
    }

    object SeenBy : NavRoute("seen_by/{noticeId}") {
        fun withId(id: String) = "seen_by/$id"
    }

    object EditNotice : NavRoute("edit_notice/{noticeId}") {
        fun withId(id: String) = "edit_notice/$id"
    }
}
