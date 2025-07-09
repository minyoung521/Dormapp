package com.example.dormapp.api

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("is_staff") val isStaff: Boolean,
    @SerializedName("is_superuser") val isSuperuser: Boolean,
    val token: String?,
    val error: String?
)

data class SignupRequest(
    val username: String,
    val password: String,
    val email: String,
    val department: String,
    @SerializedName("full_name") val fullName: String
)

data class SignupResponse(
    val success: Boolean,
    @SerializedName("user_id") val userId: Int?,
    val error: String?
)

data class DormApplyRequest(
    val name: String,
    val student_number: String,
    val gender: String,
    val content: String
)

data class DormApplyResponse(
    val success: Boolean,
    val dorm: DormData?,
    val error: String?
)

data class DormData(
    val id: Int,
    val name: String,
    val student_number: String,
    val content: String,
    val gender: String,
    val building_name: String?,
    val r_number: Int?,
    val position: Int?,
    val is_available: Boolean
)

data class OutingApplyRequest(
    val name: String,
    val student_number: String,
    val out_date: String
)

data class OutingApplyResponse(
    val success: Boolean,
    val outing: OutingData?,
    val error: String?
)

data class OutingData(
    val id: Int,
    val name: String,
    val student_number: String,
    val out_date: String,
    val applied_at: String
)

data class OutingListResponse(
    val success: Boolean,
    val today_list: List<OutingData>?,
    val error: String?
)

data class NoticeData(
    val id: Int,
    val title: String,
    val content: String,
    val date: String
)

data class NoticeListResponse(
    val success: Boolean,
    val notices: List<NoticeData>?,
    val error: String?
)

data class NoticeCreateRequest(
    val title: String,
    val content: String
)

data class NoticeCreateResponse(
    val success: Boolean,
    val notice: NoticeData?,
    val error: String?
)

data class CommentData(
    val id: Int,
    @SerializedName("author_id") val authorId: Int?,
    @SerializedName("anon_author") val anonAuthor: String?,
    val content: String,
    @SerializedName("created_at") val createdAt: String
)

data class CommentResponse(
    val success: Boolean,
    val comment: CommentData?,
    val error: String?
)

data class CommentListResponse(
    val success: Boolean,
    val comments: List<CommentData>?,
    val error: String?
)

data class PostData(
    val id: Int,
    @SerializedName("author_id") val authorId: Int,
    @SerializedName("anon_author") val anonAuthor: String,
    val title: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("image") val imageUrl: String?,
    @SerializedName("comments") val comments: List<CommentData>?
)

data class PostListResponse(
    val success: Boolean,
    val posts: List<PostData>?,
    val error: String?
)

data class PostDetailResponse(
    val success: Boolean,
    val post: PostData?,
    val error: String?
)

data class PostRequest(
    val title: String,
    val content: String
)

data class PostCreateResponse(
    val success: Boolean,
    val post: PostData?,
    val error: String?
)

data class DeleteResponse(
    val success: Boolean,
    val error: String?
)

data class UserProfileData(
    @SerializedName("user_id") val userId: Int,
    val username: String,
    val email: String,
    val department: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("reward_point") val rewardPoint: Int,
    @SerializedName("penalty_point") val penaltyPoint: Int,
    @SerializedName("is_staff") val isStaff: Boolean,
    @SerializedName("is_superuser") val isSuperuser: Boolean
)

data class MyPageData(
    val user: UserProfileData,
    val dorm: DormData?
)

data class MyPageResponse(
    val success: Boolean,
    val mypage: MyPageData?
)

data class GivePointRequest(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("point_type") val pointType: String,
    val point: Int
)

data class GivePointResponse(
    val success: Boolean,
    val profile: UserProfileData?,
    val error: String?
)

data class InquiryData(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val username: String,
    val title: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
    val answer: InquiryAnswerData?
)

data class InquiryAnswerData(
    val id: Int,
    @SerializedName("admin_id") val adminId: Int,
    @SerializedName("admin_username") val adminUsername: String,
    val answer: String,
    @SerializedName("answered_at") val answeredAt: String
)

data class InquiryListResponse(
    val success: Boolean,
    val inquiries: List<InquiryData>?,
    val error: String?
)

data class InquiryCreateRequest(
    val title: String,
    val content: String
)

data class InquiryCreateResponse(
    val success: Boolean,
    val inquiry: InquiryData?,
    val error: String?
)

data class InquiryDetailResponse(
    val success: Boolean,
    val inquiry: InquiryData?,
    val error: String?
)

data class InquiryAnswerRequest(
    val answer: String
)

data class InquiryAnswerResponse(
    val success: Boolean,
    val answer: InquiryAnswerData?,
    val error: String?
)

data class DormApplyListItem(
    val id: Int,
    val name: String,
    val student_number: String,
    val content: String,
    val gender: String,
    val building_name: String?,
    val r_number: Int?,
    val position: Int?,
    val is_available: Boolean
)

data class DormApplyListResponse(
    val success: Boolean,
    val dorms: List<DormApplyListItem>?,
    val error: String?
)


interface ApiService {
    @POST("api/login/")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/signup/")
    fun signup(@Body request: SignupRequest): Call<SignupResponse>

    @POST("api/dorm_apply/")
    fun applyDorm(@Body request: DormApplyRequest): Call<DormApplyResponse>

    @POST("api/outing_apply/")
    fun applyOuting(@Body request: OutingApplyRequest): Call<OutingApplyResponse>

    @GET("api/dorm-applications/")
    fun getDormApplyList(): Call<DormApplyListResponse>

    @PATCH("api/dorm-applications/{id}/")
    fun updateDormApply(
        @Path("id") id: Int,
        @Body params: Map<String, String>
    ): Call<DormApplyResponse>

    @DELETE("api/dorm-applications/{id}/")
    fun deleteDormApply(
        @Path("id") id: Int
    ): Call<DeleteResponse>

    @GET("api/outing_apply/today/")
    fun outingListToday(): Call<OutingListResponse>

    @GET("api/notices/")
    fun getNotices(): Call<NoticeListResponse>

    @POST("api/notices/")
    fun addNotice(@Body request: NoticeCreateRequest): Call<NoticeCreateResponse>

    @GET("api/posts/")
    fun getPosts(): Call<PostListResponse>

    @GET("api/posts/{id}/")
    fun getPost(@Path("id") postId: Int): Call<PostDetailResponse>

    @POST("api/posts/")
    fun addPost(@Body request: PostRequest): Call<PostCreateResponse>

    @PUT("api/posts/{id}/")
    fun updatePost(@Path("id") postId: Int, @Body request: PostRequest): Call<PostCreateResponse>

    @DELETE("api/posts/{id}/")
    fun deletePost(@Path("id") postId: Int): Call<DeleteResponse>

    @Multipart
    @POST("api/posts/")
    fun addPostWithImage(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<PostCreateResponse>

    @POST("api/posts/{id}/comments/")
    fun addComment(@Path("id") postId: Int, @Body body: Map<String, String>): Call<CommentResponse>

    @GET("api/posts/{id}/comments/")
    fun getComments(@Path("id") postId: Int): Call<CommentListResponse>

    @GET("api/mypage/")
    fun getMyPage(): Call<MyPageResponse>

    @POST("api/give_point/")
    fun givePoint(@Body request: GivePointRequest): Call<GivePointResponse>

    @GET("api/inquiries/")
    fun getInquiries(): Call<InquiryListResponse>

    @POST("api/inquiries/")
    fun createInquiry(@Body request: InquiryCreateRequest): Call<InquiryCreateResponse>

    @GET("api/inquiries/{id}/")
    fun getInquiryDetail(@Path("id") inquiryId: Int): Call<InquiryDetailResponse>

    @POST("api/inquiries/{id}/")
    fun answerInquiry(
        @Path("id") inquiryId: Int,
        @Body request: InquiryAnswerRequest
    ): Call<InquiryAnswerResponse>
}
