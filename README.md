# ezdelivery
# 음식 주문시스템
## 서비스 시나리오

### 기능적 요구사항
1. 고객이 메뉴를 선택하여 주문한다
1. 고객이 결제한다
1. 주문이 되면 주문 내역이 입점상점주인에게 전달된다
1. 상점주인이 주문내역 확인하여 요리를 시작한다
1. 요리가 시작되면 라이더는 배달을 준비한다
1. 배달을 준비하면서 배달료가 라이더에 지불되어야만 배달을 출발한다
1. 고객이 주문을 취소할 수 있다
1. 주문이 취소되면 배달이 취소된다
1. 고객이 주문상태를 마이페이지에서 중간중간 조회한다
1. 주문상태가 바뀔 때 마다 카톡으로 알림을 보낸다
    
### 비기능적 요구사항
    1. 트랜잭션
       - 결제가 되지 않은 신청건은 성립되지 않아야 한다.(Sync 호출)
    2. 장애격리
       - 메세지 전송 기능이 수행되지 않더라도 단말신청/결제는 365일 24시간 받을 수 있어야 한다.(Async(event-driven), Eventual Consistency)
       - 신청시스템이 과중되면 사용자를 잠시동안 받지 않고 잠시 후에 하도록 유도한다.(Circuit Breaker, Fallback)
    3. 성능
       - 신청, 결제 상태에 대해 고객은 한번에 확인할 수 있어야 한다.(CQRS)
       - 신청/취소/결제/결제취소 할 때마다 메세지로 알림을 줄 수 있어야 한다.(Event Driven)
       
### Event Storming 후 1차 완성모델


### 기능적/비기능적 요구사항을 커버하는지 검증
![image](https://user-images.githubusercontent.com/61194075/124386875-5f6e9100-dd17-11eb-8ec7-30b58187c9b5.png)

    1. 대리점에서는 판매단말을 등록/삭제 할 수 있다. → OK(노란색)
    2. 고객은 판매단말을 신청/취소 할 수 있다. → OK(빨간색)
    3. 고객은 단말신청 시 단말금액을 결제한다. → OK(파란색)
    4. 고객이 단말 신청취소 시 결제한 단말금액은 환불되어야 한다. → OK(초록색)
    5. 단말 신청/취소/결제 시 고객에게 알림을 전송한다. → OK(주황색)
    6. 판매단말의 수량이 부족한 경우 단말을 판매할 수 없다.  → ??? → 단말정보를 단말 신청시점에 체크할 수 있도록 Req/Res 추가

### 모델 수정 및 완성
![image](https://user-images.githubusercontent.com/61194075/124387065-13701c00-dd18-11eb-8ced-f510774daba9.png)
    
    #### 기능적 요구사항
    6. 판매단말의 수량이 부족한 경우 단말을 판매할 수 없다. → Req/Res로 주문 전 단말정보 조회 추가
    
    #### 비기능적 요구사항 → 모두 충족
    1. 트랜잭션
       - 결제가 되지 않은 신청건은 성립되지 않음(Sync 호출)
    2. 장애격리
       - 메세지 전송 기능이 수행되지 않더라도 단말신청/결제는 365일 24시간 받을 수 있어야 한다.(Async(event-driven), Eventual Consistency)
       - 신청시스템이 과중되면 사용자를 잠시동안 받지 않고 잠시 후에 하도록 유도한다.(Circuit Breaker, Fallback)
    3. 성능
       - 신청, 결제 상태에 대해 고객은 한번에 확인할 수 있어야 한다.(CQRS)
       - 신청/취소/결제/결제취소 할 때마다 메세지로 알림을 줄 수 있어야 한다.(Event Driven)
       
### 헥사고날 아키텍처 다이어그램 도출
![분산스트림](https://user-images.githubusercontent.com/61194075/124387260-c771a700-dd18-11eb-89e9-328403f69b18.png)

    1. 호출관계에서 Request/Response, Publish/Subscribe를 구분하여 처리함
    2. 하나의 서브도메인이 장애가 발생하더라도 다른 서브도메인이 큰 영향을 받지 않도록 설계함
    
## 구현
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 Bounded Context별로 대변되는 마이크로 서비스들을 Springboot로 구현
구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다.

    [Order] - Port : 8081
    cd order
    mvn spring-boot:run
    
    [Payment] - Port : 8082
    cd payment
    mvn spring-boot:run
    
    [CellPhone] - Port : 8083
    cd cellphone
    mvn spring-boot:run
    
    [Message] - Port : 8084
    cd message
    mvn spring-boot:run
    
    [ViewPage] - Port : 8085
    cd viewpage
    mvn spring-boot:run
       
    [Gateway] - Port : 8088
    cd gateway
    mvn spring-boot:run
    
![수정1_1](https://user-images.githubusercontent.com/61194075/124760497-c6848380-df6b-11eb-99a9-3c03d1d911e4.png)

### DDD의 적용
- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. (예시는 cellphone 마이크로 서비스)

[cellphone > src > main > java > cellphone.java]

```
package cellphone;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="CellPhone_table")
public class CellPhone {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long cellphoneId;
    private String cellphoneName;
    private Integer cellphoneCnt;
    private Integer cellphoneAmt;

    public Long getCellphoneId() {
        return cellphoneId;
    }

    public void setCellphoneId(Long cellphoneId) {
        this.cellphoneId = cellphoneId;
    }
    public String getCellphoneName() {
        return cellphoneName;
    }

    public void setCellphoneName(String cellphoneName) {
        this.cellphoneName = cellphoneName;
    }
    public Integer getCellphoneCnt() {
        return cellphoneCnt;
    }

    public void setCellphoneCnt(Integer cellphoneCnt) {
        this.cellphoneCnt = cellphoneCnt;
    }
    public Integer getCellphoneAmt() {
        return cellphoneAmt;
    }

    public void setCellphoneAmt(Integer cellphoneAmt) {
        this.cellphoneAmt = cellphoneAmt;
    }
}
```
- Spring Data REST 의 RestRepository 를 적용하여 JPA를 통해 별도 처리 없이 다양한 데이터 소스 유형을 활용가능하도록 하였으며, Entity에 @Table, @Id 어노테이션을 표시하였다.

[cellphone > src > main > java > cellphone.java]

```
package cellphone;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="cellPhones", path="cellPhones")
public interface CellPhoneRepository extends PagingAndSortingRepository<CellPhone, Long>{

}
```
- 적용 후 REST API 테스트

```
# cellPhone 서비스에서 재고생성
http POST http://localhost:8083/cellPhones cellphoneId=1 cellphoneAmt=1500000 cellphoneName=iPhone12 cellphoneCnt=10

# order 서비스에서 신청처리
http POST http://localhost:8081/orders orderId=1 cellphoneId=1 paymentId=1

# payment 서비스에서 결제 확인
http GET http://localhost:8082/payments

# cellPhone 서비스에서 재고 차감확인
http GET http://localhost:8083/cellPhones

# order 서비스에서 신청취소
http DELETE http://localhost:8081/orders/1
```

### 동기식 호출(Sync) 과 Fallback 처리
#### 동기식 호출(Sync)
- 분석단계에서의 조건 중 하나로 order → payment 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다.

[order > src > main > java > cellphone > external > PaymentService.java]

```
package cellphone.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@FeignClient(name="payment", url="http://localhost:8082")
public interface PaymentService {
    @RequestMapping(method= RequestMethod.GET, path="/payCellPhone")
    public Long payCellPhone(@RequestParam("orderId") Long orderId,
                                @RequestParam("paymentAmt") Integer paymentAmt,
                                @RequestParam("cellphoneId") Long cellphoneId);

}
```
- 신청 요청을 받은 직후(@PostPersist) 가능상태 및 결제를 동기(Sync)로 요청하도록 처리

[order > src > main > java > cellphone > Order.java]

```
@Entity
@Table(name="Order_table")
public class Order {

    .......

    @PostPersist
    public void onPostPersist(){

        Ordered ordered = new Ordered();
        Long getPayemntId = OrderApplication.applicationContext.getBean(cellphone.external.PaymentService.class).payCellPhone(this.getOrderId(), cellphoneAmt, this.getCellphoneId());

        System.out.println("\n\n####paymentId : " + getPayemntId + "\n\n");
        //payment에서 payCellPhone이 완료가 되면 응답이 올 것이고, boolean 값으로 판단 후 Response(여기)에서 최종 저장처리를 한다.
        if(getPayemntId > 0)
        {
            .......
            BeanUtils.copyProperties(this, ordered);
            ordered.publishAfterCommit();
        }
    }
```
- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, Payment가 장애가 나면 Order도 동작하지 못함을 확인

![image](https://user-images.githubusercontent.com/61194075/124389230-318e4a00-dd21-11eb-81a6-c4040ed9c2a4.png)

- Payment 기동 후에는 재처리 시 정상처리 됨을 확인

![image](https://user-images.githubusercontent.com/61194075/124389271-5e426180-dd21-11eb-9024-d031d3a8fc3d.png)

#### Fallback 처리
- Order-Payment의 Request/Response 구조에 Spring Hystrix를 사용하여 FallBack 기능 구현
1. [order > src > main > java > cellphone > external > PaymentService.java]에 configuration, fallback 옵션 추가
2. configuration 클래스 및 fallback 클래스 추가
3. [order > src > main > resources > application.yml]에 hystrix 

[order > src > main > java > cellphone > external > PaymentService.java]

```
package cellphone.external;
......
import org.springframework.stereotype.Component;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

import feign.Feign;
import feign.hystrix.HystrixFeign;
import feign.hystrix.SetterFactory;

import java.util.Date;

@FeignClient(name="payment", url="http://localhost:8082"
            , configuration=PaymentService.PaymentServiceConfiguration.class
            , fallback=PaymentService.PaymentServiceFallback.class)
            
public interface PaymentService {
    @RequestMapping(method= RequestMethod.GET, path="/payCellPhone")
    public Long payCellPhone(@RequestParam("orderId") Long orderId,
                             @RequestParam("paymentAmt") Integer paymentAmt,
                             @RequestParam("cellphoneId") Long cellphoneId);

    @Component
    class PaymentServiceFallback implements PaymentService {
        @Override
        public Long payCellPhone(Long orderId,Integer paymentAmt,Long cellphoneId){
            System.out.println("\n###PaymentServiceFallback works####\n");   // fallback 메소드 작동 테스트
            return 0L;
        }
    }

    @Component
    class PaymentServiceConfiguration {
        Feign.Builder feignBuilder(){
            SetterFactory setterFactory = (target, method) -> HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(target.name()))
            .andCommandKey(HystrixCommandKey.Factory.asKey(Feign.configKey(target.type(), method)))
            // 위는 groupKey와 commandKey 설정
            // 아래는 properties 설정
            .andCommandPropertiesDefaults(HystrixCommandProperties.defaultSetter()
                .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                .withMetricsRollingStatisticalWindowInMilliseconds(10000) // 기준시간
                .withCircuitBreakerSleepWindowInMilliseconds(3000) // 서킷 열려있는 시간
                .withCircuitBreakerErrorThresholdPercentage(50)) // 에러 비율 기준 퍼센트
                ;
            return HystrixFeign.builder().setterFactory(setterFactory);
        }        
    }
}
```
[order > src > main > resources > application.yml]
```
feign:
  hystrix:
    enabled: true
```

4. payment 서비스 중지 후 주문 시 아까전과 다르게 500에러가 아닌 수행처리 됨

![image](https://user-images.githubusercontent.com/61194075/124389921-546e2d80-dd24-11eb-9a3f-9787ca166492.png)

### 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트
payment에서  이루어 진 후(paymentApproved) cellPhone의 수량이 Update되고, 결제취소(paymentCancelled) 후 다시 수량이 증가하도록 하는 행위는 비동기식으로 처리한다.
- 결제가 승인이 되면 paymentApproved 통해 이밴트 Kafka로 전송(Publish)

[payment > src > main > java > cellphone > PolicyHandler.java]

```
@Entity
@Table(name="Payment_table")
public class Payment {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long paymentId;
    private Integer paymentAmt;
    private Long orderId;
    private Long cellphoneId;
    private String paymentStatus;

    @PrePersist
    public void onPostPersist(){
        PaymentApproved paymentApproved = new PaymentApproved();

        paymentApproved.setPaymentId(this.getPaymentId());
        paymentApproved.setOrderId(this.getOrderId());
        paymentApproved.setPaymentStatus("paymentApproved");
        paymentApproved.setPaymentAmt(paymentAmt);

        System.out.println("\n\n#####paymentApproved.getOrderId() : " + paymentApproved.getOrderId() + "\n\n");        
        System.out.println("\n\n#####paymentApproved.getPaymentStatus() : " + paymentApproved.getPaymentStatus() + "\n\n");
        
        BeanUtils.copyProperties(this, paymentApproved);
        paymentApproved.publishAfterCommit();
    }
```
- cellPhone 서비스에서는 결제 승인 이벤트(paymentApproved)를 받으면 재고 차감

[cellphone > src > main > java > cellphone > PolicyHandler.java]
```
package cellphone;

......

@Service
public class PolicyHandler{
    @Autowired CellPhoneRepository cellPhoneRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_ApplyCellPhone(@Payload PaymentApproved paymentApproved){

        if(!paymentApproved.validate()) return;

        // cellphone의 객체들 중에 특정 값을 가져옴
        java.util.Optional<CellPhone> cellPhoneOptional = cellPhoneRepository.findById(paymentApproved.getCellphoneId());
        CellPhone cellphone = cellPhoneOptional.get();

        ......

        cellphone.setCellphoneCnt(cellphone.getCellphoneCnt() - 1);
        cellPhoneRepository.save(cellphone);

        // Sample Logic //
        System.out.println("\n\n##### listener ApplyCellPhone : " + paymentApproved.toJson() + "\n\n");

    }
```
![image](https://user-images.githubusercontent.com/61194075/124391268-bd58a400-dd2a-11eb-9908-d9a24d81b69a.png)

- message 서비스는 완전히 분리가 되어있으며, 이벤트 수신이 되면 업무처리가 되기 때문에 message 서비스가 잠시 다운이 되어도 order 처리하는데 문제되지 않음

### Saga 패턴
- 각 서비스의 트랜잭션은 단일 서비스 내의 데이터를 갱신하는 일종의 로컬 트랜잭션 방법이고 서비스의 트랜잭션이 완료 후에 다음 서비스가 트리거 되어, 트랜잭션을 실행하는 패턴임
- 아래 그림과 같이 Saga패턴에 맞춘 트랜잭션 처리(빨간색), 취소 시 자동으로 Roll-Back처리(파란색)가 되도록 연쇄적인 트리거 처리를 함

![5](https://user-images.githubusercontent.com/61194075/124849576-fd917e00-dfd9-11eb-8290-7cb333329b0f.png)

### CQRS
- 고객이 신청상태, 결제상태, 결제금액 등을 조회할 수 있도록 CQRS로 구현함
- Order, Payment의 Status를 통합해서 조회하기 때문에 다른 핵심 서비스들의 성능저하 이슈를 해결할 수 있다.
- 비동기식으로 Kafka를 통해 이벤트를 수신하게 되면 별도 CQRS 테이블(CellPhoneOrderView_table)에 관리한다.

```
# 테이블 구성
@Id
@GeneratedValue(strategy=GenerationType.AUTO)
private Long orderId;
private String orderStatus;
private Long paymentId;
private String paymentStatus;
private Long cellphoneId;
private String cellphoneName;
private Integer cellphoneAmt;
```
[viewpage > src > main > java > cellphone > CellPhoneOrderViewHandler.java]
```
package cellphone;

import cellphone.config.kafka.KafkaProcessor;

......

@Service
public class CellPhoneOrderViewViewHandler {

    @Autowired
    private CellPhoneOrderViewRepository cellPhoneOrderViewRepository;
    
    //다른항목들의 이벤트가 kafka로 올라왔을 때 veiw를 위한 데이터 생성
    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrdered_then_Create(@Payload PaymentApproved paymentApproved) {

        if(!paymentApproved.validate()) return;

        CellPhoneOrderView cellPhoneOrderView = new CellPhoneOrderView();

        cellPhoneOrderView.setOrderId(paymentApproved.getOrderId());
        cellPhoneOrderView.setPaymentId(paymentApproved.getPaymentId());
        cellPhoneOrderView.setCellphoneId(paymentApproved.getCellphoneId());
        cellPhoneOrderView.setPaymentStatus(paymentApproved.getPaymentStatus());

        cellPhoneOrderViewRepository.save(cellPhoneOrderView);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenCellPhoneModified_then_Update(@Payload Ordered ordered) {
    
        if(!ordered.validate()) return;

        java.util.Optional<CellPhoneOrderView> cOptional = cellPhoneOrderViewRepository.findById(ordered.getOrderId());
        
        CellPhoneOrderView view = cOptional.get();
        view.setOrderStatus(ordered.getOrderStatus());
        view.setCellphoneAmt(ordered.getCellphoneAmt());
        view.setCellphoneName(ordered.getCellphoneName());
        view.setPaymentId(ordered.getPaymentId());

        cellPhoneOrderViewRepository.save(view);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCancelled_then_Update(@Payload PaymentCancelled paymentCancelled) {
    
        if(!paymentCancelled.validate()) return;

        java.util.Optional<CellPhoneOrderView> cOptional = cellPhoneOrderViewRepository.findById(paymentCancelled.getOrderId());

        CellPhoneOrderView view = cOptional.get();
        view.setPaymentStatus(paymentCancelled.getPaymentStatus());

        cellPhoneOrderViewRepository.save(view);
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCancelled_then_Update(@Payload OrderCancelled orderCancelled) {
        if(!orderCancelled.validate()) return;

        java.util.Optional<CellPhoneOrderView> cOptional = cellPhoneOrderViewRepository.findById(orderCancelled.getOrderId());

        CellPhoneOrderView view = cOptional.get();
        view.setOrderStatus(orderCancelled.getOrderStatus());
        
        cellPhoneOrderViewRepository.save(view);
    }

```
- 신청 3개 처리 후 1번 신청에 대해서 취소처리 한 뒤 viewpage를 확인하면 신청취소 및 결제취소상태 확인가능

![수정2_1](https://user-images.githubusercontent.com/61194075/124761390-b4efab80-df6c-11eb-881c-b8ec9d9425d5.png)

### Correlation
- 신청 및 신청취소 시 일련의 처리 과정 검증

1. 재고생성

![image](https://user-images.githubusercontent.com/61194075/124391666-b894ef80-dd2c-11eb-8453-7989210405d5.png)

2. 2번 재고인 "Samsung GalaxyS21"단말 1번 신청처리(payment, cellphone, message, viewpage 자동처리 확인)
   - payment는 req/res로 결제처리
   - cellphone은 자동으로 재고차감
   - message로 고객에게 알람전송
   - viewpage를 통해 고객이 항시 확인가능

![image](https://user-images.githubusercontent.com/61194075/124391930-f47c8480-dd2d-11eb-8fa5-4b4f212b3350.png)

3. 2번 재고인 "Samsung GalaxyS21"단말 2번 신청처리(payment, cellphone, message, viewpage 자동처리 확인)
   - 재고 차감 및 신청에 따른 message, veiwpage 확인

![image](https://user-images.githubusercontent.com/61194075/124392042-8f755e80-dd2e-11eb-82ba-a9b857d548ca.png)

4. 1번 신청처리 취소(payment, cellphone, message, viewpage 자동처리 확인)
   - payment는 req/res로 결제취소처리
   - cellphone은 자동으로 재고증가
   - message로 고객에게 알람전송
   - viewpage를 통해 고객이 항시 확인가능

![image](https://user-images.githubusercontent.com/61194075/124392072-ae73f080-dd2e-11eb-952a-44b14941d710.png)

### Gateway
- Gateway 생성을 통하여 마이크로서비스들의 진입점을 통일시킴

[gateway > src > main > resource > application.yml]

```
server:
  port: 8088

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: order
          uri: http://localhost:8081
          predicates:
            - Path=/orders/** 
        - id: payment
          uri: http://localhost:8082
          predicates:
            - Path=/payments/** 
        - id: cellphone
          uri: http://localhost:8083
          predicates:
            - Path=/cellPhones/** 
        - id: message
          uri: http://localhost:8084
          predicates:
            - Path=/messages/** 
        - id: viewpage
          uri: http://localhost:8085
          predicates:
            - Path= /cellPhoneOrderViews/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true
```

- Gateway 서비스 기동 후 각 서비스로 접근이 가능한지 확인
- Gateway Port 8088을 통해 8083 cellphone 서비스에 접근해서 재고생성 및 8081 order 서비스 접근하여 주문 및 8085 viewpage에서 주문상태 확인


### CI/CD
- 각 서비스의 경로에 buildspec.yml 파일을 구성했고, CI/CD는 AWS Codebuild를 사용하여 구현함
- 각 서비스별로 CodeBuild를 구성했으며, CodeBuild와 EKC 연결 시 필요한 환경변수를 가져옴

[order > buildspec.yml]
- k8s는 AWS에서 아직 codeDeploy 기능은 미제공하고 있으나 CodeBuild 수행 시 yml 파일 내에서 docker build 및 kubectl 명령어로 배포까지 가능함
```
version: 0.2

env:
  variables:
    _PROJECT_NAME: "final-order" #ECR의 Repository의 명칭

phases:
  install:
    runtime-versions:
      java: corretto8 #codebuild 버전 때문에 변경
      docker: 18
    commands:
      - echo install kubectl
      - curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl
      - chmod +x ./kubectl
      - mv ./kubectl /usr/local/bin/kubectl
  pre_build:
    commands:
      - echo Logging in to Amazon ECR...
      - echo $_PROJECT_NAME
      - echo $AWS_ACCOUNT_ID
      - echo $AWS_DEFAULT_REGION
      - echo $CODEBUILD_RESOLVED_SOURCE_VERSION
      - echo start command
      - $(aws ecr get-login --no-include-email --region $AWS_DEFAULT_REGION)
  build:
    commands:
      - echo Build started on `date`
      - echo Building the Docker image...
      - cd order # cellphone이 아닌 하위(cellphone > order) 경로로 이동해서 build 처리
      - mvn package -Dmaven.test.skip=true
      - docker build -t $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$_PROJECT_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION  .
  post_build:
    commands:
      - echo Pushing the Docker image...
      - docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$_PROJECT_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION
      - echo connect kubectl
      - kubectl config set-cluster k8s --server="$KUBE_URL" --insecure-skip-tls-verify=true
      - kubectl config set-credentials admin --token="$KUBE_TOKEN"
      - kubectl config set-context default --cluster=k8s --user=admin
      - kubectl config use-context default
      - |
          cat <<EOF | kubectl apply -f -
          apiVersion: v1
          kind: Service
          metadata:
            name: $_PROJECT_NAME
            labels:
              app: $_PROJECT_NAME
          spec:
            ports:
              - port: 8080
                targetPort: 8080
            selector:
              app: $_PROJECT_NAME
          EOF
      - |
          cat  <<EOF | kubectl apply -f -
          apiVersion: apps/v1
          kind: Deployment
          metadata:
            name: $_PROJECT_NAME
            labels:
              app: $_PROJECT_NAME
          spec:
            replicas: 1
            selector:
              matchLabels:
                app: $_PROJECT_NAME
            template:
              metadata:
                labels:
                  app: $_PROJECT_NAME
              spec:
                containers:
                  - name: $_PROJECT_NAME
                    image: $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$_PROJECT_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION
                    ports:
                      - containerPort: 8080
                    readinessProbe:
                      httpGet:
                        path: /actuator/health
                        port: 8080
                      initialDelaySeconds: 10
                      timeoutSeconds: 2
                      periodSeconds: 5
                      failureThreshold: 10
                    livenessProbe:
                      httpGet:
                        path: /actuator/health
                        port: 8080
                      initialDelaySeconds: 120
                      timeoutSeconds: 2
                      periodSeconds: 5
                      failureThreshold: 5
          EOF
cache:
  paths:
    - '/root/.m2/**/*'
```

[환경변수 1 - 개인계정ID 12자리]

[환경변수 2 - SA 생성]
EC2에서 아래 명령어로 KUBE_TOKEN 생성
```
#Service Account 생성
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  name: eks-admin
  namespace: kube-system
EOF

#Cluster Role 생성
cat <<EOF | kubectl apply -f -
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  name: eks-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: eks-admin
  namespace: kube-system
EOF

#TOKEN 가져오기
kubectl -n kube-system describe secret eks-admin
```
![image](https://user-images.githubusercontent.com/61194075/124426943-b458e880-dda5-11eb-94c0-0d246135605f.png)

[환경변수 3 - ECR URL 획득]
![image](https://user-images.githubusercontent.com/61194075/124433170-6647e300-ddad-11eb-8290-daa71fb3c2e3.png)

- codebuild에 환경변수 세팅

![codebuild8](https://user-images.githubusercontent.com/61194075/124532060-bc1e9880-de4a-11eb-81f7-6966e774ce9b.PNG)

- 환경변수 적용 후 GIT에서 PUSH가 일어났을 때 자동으로 CodeBuild Docker 빌드를 하고, EKS에 Pod 기동까지 확인

![codebuild9](https://user-images.githubusercontent.com/61194075/124540704-f4c66e00-de5a-11eb-8592-5d8578c26fa6.PNG)
![codebuild10](https://user-images.githubusercontent.com/61194075/124540693-ed06c980-de5a-11eb-84a6-6192cf41aab7.PNG)

- 프로젝트 내 전체 서비스 6개 모두 CodeBuild 처리 확인

![codebuild11](https://user-images.githubusercontent.com/61194075/124641434-aac8a100-dec9-11eb-8d0e-4fdf6c155b7d.PNG)
![codebuild12](https://user-images.githubusercontent.com/61194075/124641443-ac926480-dec9-11eb-93b1-8072745b78eb.PNG)
![codebuild13](https://user-images.githubusercontent.com/61194075/124641453-aef4be80-dec9-11eb-9dd0-abfbc35a74aa.PNG)

[참고사항. CodeBuild 생성]

![codebuild1](https://user-images.githubusercontent.com/61194075/124540858-348d5580-de5b-11eb-9d51-cf10c828e650.PNG)
![codebuild2](https://user-images.githubusercontent.com/61194075/124540870-38b97300-de5b-11eb-9bb2-cbc42f0d9137.PNG)
![codebuild3](https://user-images.githubusercontent.com/61194075/124540882-3fe08100-de5b-11eb-894d-2cd1af0dccbd.PNG)
![codebuild4](https://user-images.githubusercontent.com/61194075/124540889-47078f00-de5b-11eb-89e2-29f2e0f41cde.PNG)
![codebuild5](https://user-images.githubusercontent.com/61194075/124540895-48d15280-de5b-11eb-8cbf-7307ebba765a.PNG)
![codebuild6](https://user-images.githubusercontent.com/61194075/124540901-4b33ac80-de5b-11eb-9b82-5045b89e75e0.PNG)

### 서킷브레이커
- 먼저 시작하기 전에 유틸리티 설치(Helm, Kafka, Siege)
```
# Helm
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
chmod 700 get_helm.sh
kubectl --namespace kube-system create sa tiller
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller

# Kafka
helm repo update
helm repo add bitnami https://charts.bitnami.com/bitnami
kubectl create ns kafka
helm install my-kafka bitnami/kafka --namespace kafka

# Siege
kubectl apply -f - <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: siege
spec:
  containers:
  - name: siege
    image: apexacme/siege-nginx
EOF
```

- Codebuild가 편해서 쓰다보니 금방 프리티어 100분이 지나가버려서 과금이 시작됨, 서버 안에서 빌드, 배포 진행

![image](https://user-images.githubusercontent.com/61194075/124843950-0c723380-dfce-11eb-809e-14f7697a38df.png)
```
1. mvn package -B -Dmaven.test.skip=true

2. docker build -t 231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-order:v1 .
   docker build -t 231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-payment:v1 .
   docker build -t 231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-cellphone:v1 .
   docker build -t 231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-message:v1 .
   docker build -t 231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-viewpage:v1 .
   
3. docker push 231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-order:v1 
   docker push 231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-payment:v1 
   docker push 231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-cellphone:v1 
   docker push 231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-message:v1 
   docker push 231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-viewpage:v1 
   
4. kubectl create deploy order --image=231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-order:v1
   kubectl expose deploy order --type=ClusterIP --port=8080

   kubectl create deploy payment --image=231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-payment:v1
   kubectl expose deploy payment --type=ClusterIP --port=8080

   kubectl create deploy cellphone --image=231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-cellphone:v1
   kubectl expose deploy cellphone --type=ClusterIP --port=8080

   kubectl create deploy message --image=231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-message:v1
   kubectl expose deploy message --type=ClusterIP --port=8080

   kubectl create deploy viewpage --image=231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-viewpage:v1
   kubectl expose deploy viewpage --type=ClusterIP --port=8080
   
   # gateway는 type이 LoadBalancer로 해야지만 이후 테스트에서 http://order:8080/orders 같은 명령어 사용 
   kubectl create deploy gateway --image=231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-gateway:a22d08e3a0ce2e5e94d0ae53cbb6bcc6074ca016
   kubectl expose deploy gateway --type="LoadBalancer" --port=8080

5. kubectl delete deploy order
   kubectl delete service order
   
   kubectl delete deploy payment
   kubectl delete service payment
   
   kubectl delete deploy cellphone
   kubectl delete service cellphone
   
   kubectl delete deploy message
   kubectl delete service message
   
   kubectl delete deploy viewpage
   kubectl delete service viewpage
```

- Spring Spring FeignClient + Hystrix 옵션을 사용하여 테스팅 진행
신청(order) → 결제(payment) 시 연결을 REST API로 Response/Request로 구현되거 있으며, 과도한 신청으로 결제가 문제가 될 때 서킷브레이커로 장애격리

[order > src > main > resources > application.yml]
```
feign:
  hystrix:
    enabled: true
    
hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610
```

[payment > src > main > java > cellphone > Payment.java] - 딜레이 발생시킴
```
        try{
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e){
            e.printStackTrace();
        }
```

1. siege로 부하 발생(최초에는 정상으로 처리 시작됨)
``` 
kubectl exec -it siege -- /bin/bash
# 200명 사용자가 50초 동안 진입
siege -c200 -t50S -r5 -v --content-type "application/json" 'http://order:8080/orders POST {"cellphoneId":"1"}'
```
![siege11](https://user-images.githubusercontent.com/61194075/124761829-2891b880-df6d-11eb-992e-223675f29984.PNG)

2. 어느순간부터 서킷브레이커로 인해 차단 발생

![siege12](https://user-images.githubusercontent.com/61194075/124761865-347d7a80-df6d-11eb-86ab-1a684b07d957.PNG)

3. siege 부하옵션 때문인지 부하테스트 종료 시점까지 계속해서 500에러 발생

![siege13](https://user-images.githubusercontent.com/61194075/124761920-43fcc380-df6d-11eb-8d69-873761293721.PNG)
![siege3](https://user-images.githubusercontent.com/61194075/124761993-5b3bb100-df6d-11eb-8ee6-bf3675e84236.PNG)

4. 기본 사용자(-c1) 한명으로 바로 다시 테스트 시 정상처리됨 확인
``` 
siege -c1 -t10S -r5 -v --content-type "application/json" 'http://order:8080/orders POST {"cellphoneId":"1"}'
```
![siege4](https://user-images.githubusercontent.com/61194075/124644900-b9b15280-decd-11eb-82a3-eb11b08f135f.PNG)

- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, 약 24%가 성공률은 고가용성이 좋다고 할 수 없으므로 동적 Scale out(replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요함

### Autoscale (HorizontalPodAutoscaler)
1. metrics 설치

![hpa1](https://user-images.githubusercontent.com/61194075/124762265-ace43b80-df6d-11eb-9ff9-a41fd9bcf61d.PNG)

2. 리소스 설정

[order > kubernetes > deployment.yml]

![image](https://user-images.githubusercontent.com/61194075/124762476-e7e66f00-df6d-11eb-84e0-208d9a34f386.png)

3. 다시 deploy 시 정상적용 되었는지 확인(잘 적용이 안되어서 반드시 확인해야 했음)
```
# 현재 떠있는 서비스는 kubectl delete로 내리고, order > kubernetes로 이동(deployment.yml, service.yaml 파일이 있는 곳)
kubectl apply -f deployment.yml
kubectl apply -f service.yaml

# deploy 적용여부 확인
kubectl get deploy order -o yaml
```
![HPA15-1](https://user-images.githubusercontent.com/61194075/124763196-ac987000-df6e-11eb-8385-47468f95b9ee.PNG)

4. 아래 명령어로 HPA 설정
```
# CPU 사용량이 20%를 넘으면 replica를 3개까지 늘림
kubectl autoscale deployment order --cpu-percent=20 --min=1 --max=3
```

5. 터미널 하나 다시 띄워서 siege로 부하 설정
```
kubectl exec -it siege -- /bin/bash
siege -c200 -t50S -r5 -v --content-type "application/json" 'http://order:8080/orders POST {"cellphoneId":"1"}'
```
수행결과 서킷브레이커보다 조금 더 나은 결과를 가지고 옴
![HPA14](https://user-images.githubusercontent.com/61194075/124764417-e3bb5100-df6f-11eb-9b83-f0e1cedc8265.PNG)

6. 부하가 발생하고, replica가 3까지 늘어난 것 확인

![HPA12-1](https://user-images.githubusercontent.com/61194075/124764988-7f4cc180-df70-11eb-9297-80f0599b5060.PNG)

- kubectl get pod, hpa 명령어로도 확인가능(248%R까지 CPU 사용량 늘어남)

![HPA11-1](https://user-images.githubusercontent.com/61194075/124764767-457bbb00-df70-11eb-9fc3-9b26ee198a31.PNG)

- siege 부하가 마무리되면서 CPU 사용량이 내려오고, oder Pod는 3개가 떠서 Running 상태가 됨

![HPA13](https://user-images.githubusercontent.com/61194075/124765814-524cde80-df71-11eb-93df-e7645b2c1961.PNG)

7. 생각보다 나은결과가 나오지 않는 것 같아서 좀 더 replica 수를 늘려서 재수행처리
```
kubectl autoscale deployment order --cpu-percent=70 --min=1 --max=5
```
- 부하 발생하면서 CPU 사용량이 늘어난 것 확인되며, Pod 수도 4개로 늘려서 이미지 올리는 중

![HPA16](https://user-images.githubusercontent.com/61194075/124766683-17977600-df72-11eb-9400-662725f3a0de.PNG)

- 추가된 Pod 이미지가 올라와서 수행

![HPA17-1](https://user-images.githubusercontent.com/61194075/124766822-38f86200-df72-11eb-834a-dd8c66499341.PNG)

- kubectl get deploy order -w로 모니터링 진행

![HPA18](https://user-images.githubusercontent.com/61194075/124767092-75c45900-df72-11eb-99bb-725aed6452f0.PNG)

8. 부하가 끝나서 시간이 경과됨에 따라 다시 Pod를 내리기 시작함

![HPA19-1](https://user-images.githubusercontent.com/61194075/124767380-ba4ff480-df72-11eb-8e36-1f8eb3db20da.PNG)

9. 다시 Pod 1개인 상태로 원복

![HPA20-1](https://user-images.githubusercontent.com/61194075/124767634-f2efce00-df72-11eb-93bb-6612cb2b1464.PNG)

- kubectl get deploy order -w로 모니터링 진행

![HPA21](https://user-images.githubusercontent.com/61194075/124767647-f5eabe80-df72-11eb-870a-681ef423379c.PNG)

### Zero-downtime deploy (Readiness Probe)
1. 먼저 Readiness를 주석처리하여 사전 테스트 준비

[order > kubernetes > deployment.yml]
![readi2-1](https://user-images.githubusercontent.com/61194075/124768821-f20b6c00-df73-11eb-823a-d5c1ff9a419d.PNG)

2. 해당 yml 파일로 apply 진행한 뒤 적용여부 확인(Readiness 미존재)
```
# order > kubernetes 이동
kubectl apply -f deployment.yml

# 적용여부 확인
kubectl get deploy order -o yaml
```
![readi1](https://user-images.githubusercontent.com/61194075/124769095-2a12af00-df74-11eb-881c-74e8956badc9.PNG)

3. siege로 접속하여 부하를 넣고, 신규버전으로 다시 update 진행 시 서비스 중단 확인
``` 
kubectl set image deploy order order=231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-order:v2
```
![readi3-1](https://user-images.githubusercontent.com/61194075/124769619-942b5400-df74-11eb-826a-88bd24509617.PNG)

4. update가 진행되고 다시 서비스가 정상이 되면서 정상처리 확인

![readi4-1](https://user-images.githubusercontent.com/61194075/124769720-aa391480-df74-11eb-9f50-0bb532b1fb5c.PNG)
![readi5](https://user-images.githubusercontent.com/61194075/124769754-b1f8b900-df74-11eb-9c10-dba1595ed037.PNG)

5. 다시 deployment.yml 파일에서 Readiness 주석처리한 부분 제외하여 apply 처리함
```
# order > kubernetes 이동하여 Readiness 부분 주석 제거한 뒤 apply
kubectl apply -f deployment.yml

# 적용여부 확인
kubectl get deploy order -o yaml
```

![readi6-1](https://user-images.githubusercontent.com/61194075/124770238-25022f80-df75-11eb-9174-7304321c1fe1.PNG)

6. 다시 siege로 들어와서 부하발생 한 뒤 신규버전으로 update 진행 시 서비스 무중단으로 업무처리 됨을 확인
``` 
kubectl set image deploy order order=231047593658.dkr.ecr.ap-northeast-2.amazonaws.com/final-order:v1
```

![readi8-1](https://user-images.githubusercontent.com/61194075/124770794-980ba600-df75-11eb-9482-c10c4c7eb6a5.PNG)
![readi7-1](https://user-images.githubusercontent.com/61194075/124770801-993cd300-df75-11eb-8983-d4171d34b64b.PNG)

### Config Map/ Persistence Volume
- Database 연결 및 Secret 설정은 재고인 cellphone 서비스에 설정함

[cellphone > src > main > resource > application.yml]

![image](https://user-images.githubusercontent.com/61194075/124773295-c8ecda80-df77-11eb-8efd-e75e50336cc9.png)

[cellphone > kubernetes > deployment.yml]
- 여기서 Password는 Secret에서 비밀번호를 가져오도록 적용

![image](https://user-images.githubusercontent.com/61194075/124773483-f6398880-df77-11eb-9916-8bcd11816518.png)

- secret객체를 설정하기 위한 yaml파일을 만들어서 설정
- 아래 이미지에서 보이듯이 admin을 BASE64 형으로 인코딩한 값으로 생성함
```
kubectl create -f sql-secret.yaml
# secret 생성 확인
kubectl get secrets
```
![con1](https://user-images.githubusercontent.com/61194075/124773747-2f71f880-df78-11eb-9d6b-cd1ea94c36ec.PNG)

- cellphone 서비스를 다시 deploy 처리하고, 해당 pod에 접근하여 env 명령어로 제대로 secret 환경변수가 적용되었는지 확인

![con3-1](https://user-images.githubusercontent.com/61194075/124774637-ed958200-df78-11eb-8277-c18553ed6a56.PNG)
![con2-1](https://user-images.githubusercontent.com/61194075/124774465-cc349600-df78-11eb-95d8-b9084b6f00ec.PNG)

- MySQL을 위한 Pod 생성을 위해 yaml 파일 생성 후 기동 및 Pod 활성화 확인

![con4-1](https://user-images.githubusercontent.com/61194075/124776043-179b7400-df7a-11eb-9b8a-e0c02b8b2a40.PNG)

- MySQL Pod로 진입하여 작동 확인

![con5](https://user-images.githubusercontent.com/61194075/124776140-297d1700-df7a-11eb-8250-1ebceabd108a.PNG)

- k8s DNS 체계에서 접근가능하게 하기 위해 ClusterIP로 서비스 생성

![con6-1](https://user-images.githubusercontent.com/61194075/124776335-53ced480-df7a-11eb-9006-07ed25179465.PNG)

- Pod가 오류로 종료되어버리면 데이터의 유실이 발생함. 이를 해결하기 위해 PersistenceVolume으로 된 파일시스템에 연결함
- sql-secret.yaml 수정

![con10-1](https://user-images.githubusercontent.com/61194075/124776661-94c6e900-df7a-11eb-8e0d-9924f1727a34.PNG)

- 코드 내 aws-ebs라는 PVC가 존재하지 않아서 Pod의 상태를 보면 Pending 상태로 대기중임

![con7-1](https://user-images.githubusercontent.com/61194075/124776842-b922c580-df7a-11eb-86f7-fb94fc26d804.PNG)

- 이를 해결하기 위해서 yaml 파일을 만들어서 PVC 생성함 이후 mysql Pod의 상태가 변함을 확인

![con8](https://user-images.githubusercontent.com/61194075/124776988-db1c4800-df7a-11eb-80fb-8c595b66d3df.PNG)
![image](https://user-images.githubusercontent.com/61194075/124777305-13bc2180-df7b-11eb-8f98-8104b08adaa7.png)
![con9-1](https://user-images.githubusercontent.com/61194075/124777157-f9824380-df7a-11eb-9957-b39af6f448c2.PNG)

### Polyglot

- RDS를 생성하여 cellphone 서비스가 RDS의 MySQL을 사용할 수 있도록 함

1. RDS 생성

![poligroyt4](https://user-images.githubusercontent.com/61194075/124867957-0003d000-dffa-11eb-9689-3890322ad608.PNG)
![image](https://user-images.githubusercontent.com/61194075/124868345-9801b980-dffa-11eb-83ae-cfc1606db330.png)

2. 해당 RDS에 접근하기 위해 보안그룹을 확대해서 적용

![poligroyt5-1](https://user-images.githubusercontent.com/61194075/124867970-07c37480-dffa-11eb-9723-85c1caf5b258.PNG)

3. RDS Database에 해당되는 앤드포인트를 확인(이후 접근할 때 사용)

![poligroyt4_1-1](https://user-images.githubusercontent.com/61194075/124868054-29bcf700-dffa-11eb-8421-05e95e71de21.PNG)

4. MySQL 설치

![poligroyt6-1](https://user-images.githubusercontent.com/61194075/124868395-aa7bf300-dffa-11eb-9c79-496dc9674f9f.PNG)

5. 3번에서 확인한 엔드포인트를 통해 DB접속 확인

![poligroyt7-1](https://user-images.githubusercontent.com/61194075/124868125-4a854c80-dffa-11eb-86e9-eb1d4ad5da8c.PNG)

6. 이후 서비스에서 해당 RDS를 통해 접근하도록 진행

### Self-healing (Liveness Probe)
1. yml 파일에 Liveness 적용

![live0-1](https://user-images.githubusercontent.com/61194075/124771919-837bdd80-df76-11eb-8859-e8375274031f.png)

2. yml파일 apply적용 및 deploy에 적용확인
```
# order > kubernetes 이동하여 Liveness yml파일에 있는 것 확인한 뒤 apply 진행
kubectl apply -f deployment.yml

# 적용여부 확인
kubectl get deploy order -o yaml
```

![live0-0](https://user-images.githubusercontent.com/61194075/124771947-88409180-df76-11eb-8e9a-a0619c7a223a.png)

3. Pod가 에러로 종료가 될 때까지 siege로 계속해서 부하 발생
```
kubectl exec -it siege -- /bin/bash
siege -c200 -t120S -r5 -v --content-type "application/json" 'http://order:8080/orders POST {"cellphoneId":"1"}'
```

![live1](https://user-images.githubusercontent.com/61194075/124772213-c938a600-df76-11eb-86f7-d4e79f178cdc.PNG)

4. 다시 Pod가 재기동이 발생을 하고, 정상화 확인

![live2](https://user-images.githubusercontent.com/61194075/124772320-e3728400-df76-11eb-8b6a-54859d6a8fc5.PNG)

5. kubectl get po -w로 모니터링하면서 RESTART로 재기동이 발생하는 모습 확인

![live3-1](https://user-images.githubusercontent.com/61194075/124772499-0d2bab00-df77-11eb-8ace-273c840770f4.PNG)
![image](https://user-images.githubusercontent.com/61194075/124392415-66ee6400-dd30-11eb-9a7e-fc0613a9534a.png)
