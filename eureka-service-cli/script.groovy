@EnableEurekaServer
class EurekaServer {

    @Autowired
    void setMessage (@Value('${message}') String msg){
        System.out.println( "message = " + msg)
    }
}
