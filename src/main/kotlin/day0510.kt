import com.sun.org.apache.xpath.internal.operations.Bool
import java.net.PasswordAuthentication
import java.text.SimpleDateFormat

val articles = mutableListOf<Article>()
var articleLastId = 0

fun main() {
    println(" == 게시판 관리 프로그램 시작 == ")


    memberRepository.makeTestMembers()

   var isLoginId = false
makeTestArticle()

    while (true) {
        val prompt = if (isLoginId) {
        "로그인됨)"
        } else {
            "명령어)"
    }
        print(prompt)
        val command = readLineTrim()
        val rq =Rq(command)
        when (rq.actionPath) {
            "/system/exit" -> {
                println("프로그램을 종료합니다.")
                break
            }
            "/member/join" -> {

                print("로그인 아이디 :")
                val loginId = readLineTrim()

                val isjoinableLoginId = memberRepository.isjoinableLoginById(loginId)

                if (isjoinableLoginId == false) {
                    println("`$loginId`(은)는 이미 사용중인 로그인아이디 입니다.")
                    continue
                }


                if ( isjoinableLoginId == false) {
                    print("${loginId}(은)는 이미 사용중인 로그인아이디 입니다.")
                    continue
                }

                print("로그인비밀번호 : ")
                val password = readLineTrim()
                print("이름 : ")
                val name = readLineTrim()
                print("별명 : ")
                val nick = readLineTrim()
                print("휴대전화번호 : ")
                val phone = readLineTrim()
                print("이메일 : ")
                val email = readLineTrim()

                val id = memberRepository.isjoinableLoginById(loginId)

                println("${id}번 회원으로 가입되었습니다.")
            }

           "/article/write" -> {
                print("제목 : ")
                val title = readLineTrim()
                print("내용 : ")
                val body = readLineTrim()

                val id = addArticle(title,body)

                println("${id}번 게시물이 작성되었습니다.")
            }
            "/article/list"-> {
                val commandBits = command.trim().split(" ")

                var page = 1
                var searchKeywords = ""

                if (commandBits.size == 4){
                    searchKeywords = commandBits[2]
                    page = commandBits[3].toInt()
                } else if (commandBits.size ==3) {
                    page = commandBits[3].toInt()
                }

                val itemsCountAIntPage = 5
                val offsetCount = (page -1) * itemsCountAIntPage

                val filteredArticles = getfileredArticles(searchKeywords, offsetCount, itemsCountAIntPage)

                println(" 번호 / 작성날짜 / 제목 ")

                for (articles in articles) {
                    println("${articles.id} /${articles.regDate} / ${articles.title} / ")
                }
            }
          "/article/delete" -> {
                val id = rq.getIntParam("id",0)
                val articleToDelete = getArticleById(id)

                if (articleToDelete == null) {
                    println("${id}번 게시물이 존재하지 않습니다.")
                    continue
                }
                articles.remove(articleToDelete)
                println("${id}번 게시물이 삭제되었습니다.")
            }
   "/article/modify" -> {
                val id = command.trim().split(" ")[2].toInt()
                val articleToModify = getArticleById(id)

                if (articleToModify == null) {
                    println("${id}번 게시물이 존재하지 않습니다.")
                    continue
                }
                print("수정할 새 제목 : ")
                articleToModify.title = readLineTrim()
                print("수정할 새 내용 : ")
                articleToModify.body = readLineTrim()
                articleToModify.updateDate = Util.getNowDateStr()

                println("${id}번 게시물이 수정되었습니다.")
            }
         "/article/detail" -> {
                val id = command.trim().split(" ")[2].toInt()
                val article = getArticleById(id)

                if (article == null) {
                    print(" ${id}번 게시물이 존재하지 않습니다.")
                    continue
                }
                println("번호 : ${article.id}")
                println("작성날짜 : ${article.regDate}")
                println("갱신날짜 : ${article.updateDate}")
                println("제목 : ${article.title}")
                println("내용 : ${article.body}")

            }
        }
    }
    println(" == 게시판 관리 프로그램 종료 == ")
}

fun getfileredArticles(searchKeywords: String, offsetCount: Int, takeCount: Int): List<Article> {
    var filtered1Article = articles

    if (searchKeywords.isNotEmpty()) {
        filtered1Article = mutableListOf()

        for (article in articles) {
            if (article.title.contains(searchKeywords)) {
                filtered1Article.add(article)
            }
        }
    }

    var startIndex = filtered1Article.lastIndex - offsetCount
    var endIndex = startIndex - (takeCount + 1)

    if ( endIndex < 0) {
        endIndex = 0
    }

     val filtered2Article = mutableListOf<Article>()

    for (i in startIndex downTo endIndex) {
        filtered2Article.add(filtered1Article[i])
    }

    return filtered2Article
}


fun getArticleById(id: Int): Article? {
    for (article in articles) {
        if ( article.id == id) {
            return article
        }
    }
    return null
}



data class Article (
    val id : Int,
    val regDate : String,
    var updateDate : String,
    var title : String,
    var body : String
        )

data class Member (
    val id : Int,
    val regDate: String,
    val updateDate: String,
    val loginId : String,
    val password : String,
    val name : String,
    val nick : String,
    val phone: String,
    val email : String
        )

object memberRepository {
    private val members = mutableListOf<Member>()
    private var lastId = 0

    fun join (
        loginId: String,
        password: String,
        name: String,
        nick: String,
        phone: String,
        email: String
    ):Int {
        val regDate = Util.getNowDateStr()
        val updateDate = Util.getNowDateStr()
        val id = ++lastId

        members.add(Member(id, regDate, updateDate, loginId, password, name, nick, phone, email))
        return id
    }

    fun isjoinableLoginById(loginId: String): Boolean {
        val member = getMemberByLoginId(loginId)

        return member ==null

    }

    private fun getMemberByLoginId(loginId: String): Member? {
        for ( member in members) {
            if (member.loginId == loginId) {
                return member
            }
        }
        return null
    }
    fun makeTestMembers() {
        for (id in 0..9) {
            join("user${id}", "user${id}","user${id}_이름", "user${id}_별명", "0101234123${id}", "user${id}@test.com")
        }
    }

}
fun addArticle(title : String, body:String) {
    val id = articleLastId + 1
    val regDate = Util.getNowDateStr()
    val updateDate = Util.getNowDateStr()

    articleLastId = id
    val article = Article(id, regDate, updateDate, title, body)
    articles.add(article)

}

class Rq(command: String) {
    // 데이터 예시
    // 전체 URL : /artile/detail?id=1
    // actionPath : /artile/detail
    val actionPath: String

    // 데이터 예시
    // 전체 URL : /artile/detail?id=1&title=안녕
    // paramMap : {id:"1", title:"안녕"}
    private val paramMap: Map<String, String>

    // 객체 생성시 들어온 command 를 ?를 기준으로 나눈 후 추가 연산을 통해 actionPath와 paramMap의 초기화한다.
    // init은 객체 생성시 자동으로 딱 1번 실행된다.
    init {
        // ?를 기준으로 둘로 나눈다.
        val commandBits = command.split("?", limit = 2)

        // 앞부분은 actionPath
        actionPath = commandBits[0].trim()

        // 뒷부분이 있다면
        val queryStr = if (commandBits.lastIndex == 1 && commandBits[1].isNotEmpty()) {
            commandBits[1].trim()
        } else {
            ""
        }

        paramMap = if (queryStr.isEmpty()) {
            mapOf()
        } else {
            val paramMapTemp = mutableMapOf<String, String>()

            val queryStrBits = queryStr.split("&")

            for (queryStrBit in queryStrBits) {
                val queryStrBitBits = queryStrBit.split("=", limit = 2)
                val paramName = queryStrBitBits[0]
                val paramValue = if (queryStrBitBits.lastIndex == 1 && queryStrBitBits[1].isNotEmpty()) {
                    queryStrBitBits[1].trim()
                } else {
                    ""
                }

                if (paramValue.isNotEmpty()) {
                    paramMapTemp[paramName] = paramValue
                }
            }

            paramMapTemp.toMap()
        }
    }

    fun getStringParam(name: String, default: String): String {
        return paramMap[name] ?: default
    }

    fun getIntParam(name: String, default: Int): Int {
        return if (paramMap[name] != null) {
            try {
                paramMap[name]!!.toInt()
            } catch (e: NumberFormatException) {
                default
            }
        } else {
            default
        }
    }
}


fun makeTestArticle() {
    for (id in 0..25) {
        addArticle("제목_${id}","내용_${id}")
    }
}
fun readLineTrim() = readLine()!!.trim()

object Util {
    fun getNowDateStr(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        return format.format(System.currentTimeMillis())
    }
}