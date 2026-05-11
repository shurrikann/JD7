import android.os.Parcel
import android.os.Parcelable

// User 数据类实现 Parcelable 接口
data class User(var username: String, var pwd: String) : Parcelable {

    // 构造函数从 Parcel 中读取数据
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",  // 读取字符串，如果为 null 则使用默认值 ""
        parcel.readString() ?: ""   // 读取密码字符串
    )

    // 将数据写入 Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(pwd)
    }

    // 描述内容，通常返回 0
    override fun describeContents(): Int = 0

    // 用于从 Parcel 中创建 User 对象
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<User> {
            override fun createFromParcel(parcel: Parcel): User {
                return User(parcel)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }
    }
}
