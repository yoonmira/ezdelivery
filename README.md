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
       - 결제기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다.(Async(event-driven), Eventual Consistency)
       - 결제시스템, 배달료지급 시스템이 이 과중되면 사용자를 잠시동안 받지 않고 잠시 후에 하도록 유도한다.(Circuit Breaker, Fallback)
    3. 성능
       - 주문, 결제 상태에 대해 고객은 한번에 확인할 수 있어야 한다.(CQRS)
       - 주문/취소/결제/결제취소 할 때마다 메세지로 알림을 줄 수 있어야 한다.(Event Driven)
       
### Event Storming 후 완성모델
![모델링1](https://user-images.githubusercontent.com/84304041/124983596-1f8b0f00-e073-11eb-9d02-de8be3ae03e4.PNG)


### 기능적/비기능적 요구사항을 커버하는지 검증
![모델링1_LI](https://user-images.githubusercontent.com/84304041/124983991-a5a75580-e073-11eb-84d6-8d1ec4eea759.jpg)

    #### 기능적 요구사항
    1. 고객이 메뉴를 주문혹은 취소한다→ OK
    2. 고객이 결제혹은 결제취소된다 → OK
    3. 주문이 되면 알림을보낸다 → OK
    4. 가게주인이 주문을 확인하고 요리를 한다 → OK
    5. 요리를 하면 배달원이 배달준비를하고 배달료를받으면 배달을 출발한다. → OK
  

    
    #### 비기능적 요구사항 → 모두 충족
    1. 트랜잭션
       - 결제가 되지 않은 신청건은 성립되지 않음(Sync 호출)
       - 배달료가 지불되어 야만 라이더는 배달을 출발함
    2. 장애격리
       - 메세지 전송 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다.(Async(event-driven), Eventual Consistency)
       - 신청시스템이 과중되면 사용자를 잠시동안 받지 않고 잠시 후에 하도록 유도한다.(Circuit Breaker, Fallback)
    3. 성능
       - 주문, 결제 상태에 대해 고객은 한번에 확인할 수 있어야 한다.(CQRS)
       - 주문/취소/결제/결제취소 할 때마다 메세지로 알림을 줄 수 있어야 한다.(Event Driven)
       
### 헥사고날 아키텍처 다이어그램 도출


    1. 호출관계에서 Request/Response, Publish/Subscribe를 구분하여 처리함
    2. 하나의 서브도메인이 장애가 발생하더라도 다른 서브도메인이 큰 영향을 받지 않도록 설계함
    
    ![헥사고날](https://user-images.githubusercontent.com/84304041/125010403-65f76280-e0a1-11eb-982f-bca83e191aa4.PNG)

    
## 구현
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 Bounded Context별로 대변되는 마이크로 서비스들을 Springboot로 구현
구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다.

    [store] - Port : 8081
    cd store
    mvn spring-boot:run
    
    [order] - Port : 8082
    cd order
    mvn spring-boot:run
    
    [payment] - Port : 8083
    cd payment
    mvn spring-boot:run
    
    [alarm] - Port : 8084
    cd alarm
    mvn spring-boot:run
    
    [mypage] - Port : 8085
    cd mypage
    mvn spring-boot:run
       
    [delivery] - Port : 8086
    cd delivery
    mvn spring-boot:run
    
    [cook] - Port : 8087
    cd cook
    mvn spring-boot:run
       
    [deliverycharge] - Port : 8088
    cd deliverycharge
    mvn spring-boot:run
    
  
      


### DDD의 적용
- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. (예시는 cook 마이크로 서비스)


```
package ezdelivery;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name="Cook_table")
public class Cook {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long storeId;
    private Long orderId;
    private String menuName;
    private Long orderNumber;
    private String status;

    public FoodCooked(){
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getStatus() {
        return status;
    }



}
```
- Spring Data REST 의 RestRepository 를 적용하여 JPA를 통해 별도 처리 없이 다양한 데이터 소스 유형을 활용가능하도록 하였으며, Entity에 @Table, @Id 어노테이션을 표시하였다.

[CookRepository.java]

```
package ezdelivery;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="cooks", path="cooks")
public interface CookRepository extends PagingAndSortingRepository<Cook, Long>{


}
```
- 적용 후 REST API 테스트

```
# cook 서비스에서 요리시작
http POST localhost:8080/cooks orderId=1 menuName="후라이드" orderNumber=1 status="요리완료."



# mypage 서비스에서 진행사항 확인
http GET localhost:8080/mypges

```

### 동기식 호출(Sync) 

- 분석단계에서의 조건 중 하나로 delivery → deliverycharge 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다.

[DeliveryChargeSerive.java]

```

package ezdelivery.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

//@FeignClient(name="DeliveryCharge", url="http://localhost:8088")
@FeignClient(name="DeliveryCharge", url="${api.url.DeliveryCharge}")
public interface DeliveryChargeService {

    @RequestMapping(method= RequestMethod.GET, path="/deliveryCharges")
    public void provideCharge(@RequestBody DeliveryCharge deliveryCharge);

}
```
- 배달준비 직후(@PostPersist) 배달료지급을 동기(Sync)로 요청하도록 처리

[Delivery.java]

```

    @PostPersist
    public void onPostPersist(){

        ezdelivery.external.DeliveryCharge deliveryCharge = new ezdelivery.external.DeliveryCharge();
        BeanUtils.copyProperties(this, deliveryCharge);
        deliveryCharge.setOrderId(orderId);
        deliveryCharge.setStauts("배달료지불");

        try {
            DeliveryApplication.applicationContext.getBean(ezdelivery.external.DeliveryChargeService.class).provideCharge(deliveryCharge);
        }catch(Exception e){
            throw new RuntimeErrorException(null, "배달료지불 실패 입니다"+e.getLocalizedMessage());
        }

        //-----------------------
        //라이더에게 배달료 지불되면 최종적으로 배달 시작 이벤트발생
        //-------------------------

        DeliveryStarted deliveryStarted = new DeliveryStarted();
        BeanUtils.copyProperties(this, deliveryStarted);    
        deliveryStarted.setStatus("배달중");
        
        deliveryStarted.publishAfterCommit();
```
- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, Deliverycharge가 장애가 나면 Delivery도 동작하지 못함을 확인

![딜리버리찰지없음](https://user-images.githubusercontent.com/84304041/124989754-6e887280-e07a-11eb-8df4-6b8721dc9f22.PNG)
![딜리버리찰지없음2](https://user-images.githubusercontent.com/84304041/124989766-72b49000-e07a-11eb-9541-9e3e91e6f982.PNG)

- deliverycharge 기동 후에는 재처리 시 정상처리 됨을 확인

![딜리버리찰지없음3](https://user-images.githubusercontent.com/84304041/124989855-8f50c800-e07a-11eb-8047-3bc36d35b9e8.PNG)


### Saga 패턴
- 각 서비스의 트랜잭션은 단일 서비스 내의 데이터를 갱신하는 일종의 로컬 트랜잭션 방법이고 서비스의 트랜잭션이 완료 후에 다음 서비스가 트리거 되어, 트랜잭션을 실행하는 패턴임
- 아래 그림과 같이 Saga패턴에 맞춘 트랜잭션 처리(빨간색) 
되도록 연쇄적인 트리거 처리를 함

![saga](https://user-images.githubusercontent.com/84304041/124990703-ad6af800-e07b-11eb-93e5-f48c9835a00b.PNG)

요리사가 주문 확인후 요리를 시작하면 라이더는 배달단계에 진입함.
![배달중](https://user-images.githubusercontent.com/84304041/124990849-df7c5a00-e07b-11eb-901a-3d70e9d6166b.PNG)

### CQRS 
- 주문,배달상태를 조회할 수 있도록 CQRS로 구현함
- 비동기식으로 Kafka를 통해 이벤트를 수신하는 마이페이지 제공
-
[MypageViewHandler.java]

```
    @StreamListener(KafkaProcessor.INPUT)
    public void whenFoodCooked_then_CREATE_3 (@Payload FoodCooked foodCooked) {
        try {

            if (!foodCooked.validate()) return;

            // view 객체 생성
            Mypage mypage = new Mypage();
            // view 객체에 이벤트의 Value 를 set 함
            mypage.setOrderId(FoodCooked.getOrderId);
            mypage.setMenuName(foodCooked.getMenuName());
            mypage.setStatus("요리완료.");
            mypage.setOrderNumber(foodCooked.getOrderNumber());
            // view 레파지 토리에 save
            mypageRepository.save(mypage);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryStarted_then_CREATE_4 (@Payload DeliveryStarted deliveryStarted) {
        try {

            if (!deliveryStarted.validate()) return;

            // view 객체 생성
            Mypage mypage = new Mypage();
            // view 객체에 이벤트의 Value 를 set 함
            mypage.setOrderId(deliveryStarted.getOrderId());
            mypage.setStatus("배달중");
            // view 레파지 토리에 save
            mypageRepository.save(mypage);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }

```
![마이페이지](https://user-images.githubusercontent.com/84304041/124991880-2e76bf00-e07d-11eb-8168-8d0ae5c48897.PNG)

### Correlation
- policy handler에서 처리시 어떤건에 대한 처리인지를 구별하기위한 correlation-key 구현을 이벤트 클래스 안의 변수로 전달받아 서비스간 연관된 처리를 정확하게 구현함.
- 요리서비스에서 주인장이 주문수락을(acceptOrder)을 하여 요리시작을 하면(foodCooked)를 하게되면 배달서비스에선 이를 수신하여 라이더가 배달시작을 하게된다.
- ![주문수락요리하기](https://user-images.githubusercontent.com/84304041/124993804-d5f4f100-e07f-11eb-9063-3964c4ac8193.PNG)
![배달중](https://user-images.githubusercontent.com/84304041/124993821-dc836880-e07f-11eb-9c9d-5ce4358e3bd7.PNG)


### Gateway
- Gateway 스프링부트 app 추가후 application.yaml 내에 각 마이크로서비스의 루트를 추가하고 gateway서버의 포트를 8080으로 설정함.
 application.yml]

```
server:
  port: 8080

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: store
          uri: http://localhost:8081
          predicates:
            - Path=/stores/**
        - id: order
          uri: http://localhost:8082
          predicates:
            - Path=/orders/** 
        - id: payment
          uri: http://localhost:8083
          predicates:
            - Path=/payments/** 
        - id: alarm
          uri: http://localhost:8084
          predicates:
            - Path=/msgs/** 
        - id: mypage
          uri: http://localhost:8085
          predicates:
            - Path=/reviews/**, /mypages/**
        - id: delivery
          uri: http://localhost:8086
          predicates:
            - Path=/deliveries/** 
        - id: cook
          uri: http://localhost:8087
          predicates:
            - Path=/cooks/**       
        - id: deliverycharge
          uri: http://localhost:8088
          predicates:
            - Path=/deliverycharges/**                     
#html 경로는 root path로 맨 나중에 위치함
        - id: html
          uri: http://localhost:8081
          predicates:
            - Path=/**
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


---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: store
          uri: http://store:8080
          predicates:
            - Path=/stores/**
        - id: html
          uri: http://html:8080
          predicates:
            - Path=/**
        - id: order
          uri: http://order:8080
          predicates:
            - Path=/orders/** 
        - id: payment
          uri: http://payment:8080
          predicates:
            - Path=/payments/** 
        - id: alarm
          uri: http://alarm:8080
          predicates:
            - Path=/msgs/** 
        - id: mypage
          uri: http://mypage:8080
          predicates:
            - Path=/reviews/** /mypages/**
        - id: delivery
          uri: http://delivery:8080          
          predicates:
            - Path=/deliveries/** 
        - id: cook
          uri: http://cook:8080          
          predicates:
            - Path=/cooks/**      
        - id: deliverycharge
          uri: http://deliverycharge:8080          
          predicates:
            - Path=/deliveryCharges/**             

#html 경로는 root path로 맨 나중에 위치함
        - id: html
          uri: http://html:8080
          predicates:
            - Path=/**
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

server:
  port: 8080
```

- Gateway 서비스 기동 후 각 서비스로 접근이 가능한지 확인

![게이드웨이](https://user-images.githubusercontent.com/84304041/124999715-bb277a00-e089-11eb-8231-286bf4655235.PNG)



### CI/CD
- 각 서비스의 경로에 buildspec.yml 파일을 구성했고, CI/CD는 AWS Codebuild를 사용하여 구현함
- 각 서비스별로 CodeBuild를 구성했으며, CodeBuild와 EKC 연결 시 필요한 환경변수를 가져옴

[gateway > buildspec.yml]
- k8s는 AWS에서 아직 codeDeploy 기능은 미제공하고 있으나 CodeBuild 수행 시 yml 파일 내에서 docker build 및 kubectl 명령어로 배포까지 가능함
```
version: 0.2

env:
  variables:
    _PROJECT_NAME: "ezdelivery-gateway" #ECR의 Repository의 명칭

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
      - cd gateway # cellphone이 아닌 하위(cellphone > order) 경로로 이동해서 build 처리
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


[ECR URL 획득]
![프라이빗리파지토리](https://user-images.githubusercontent.com/84304041/124994069-3f74ff80-e080-11eb-8226-d279f008a85b.PNG)

[코드빌드 실행]
![코드빌드1](https://user-images.githubusercontent.com/84304041/124994286-95e23e00-e080-11eb-95ae-571707798e1d.PNG)

### 서킷브레이커

- Spring Spring FeignClient + Hystrix 옵션을 사용하여 테스팅 진행
Delivery→ DeliveryCharge 시 연결을 REST API로 Response/Request로 구현되거 있으며, 과도한 신청으로 배달료지급에 문제가 될 때 서킷브레이커로 장애격리

[application.yml]

0.6초 지나면 딜리버리 서비스는 더이상 기다리지말고 죽어라!
![히스트릭스1](https://user-images.githubusercontent.com/84304041/124994797-58ca7b80-e081-11eb-97c9-45560d9548c5.PNG)


[DeliveryCharge.java] - 딜레이 발생시킴
```
        try{
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e){
            e.printStackTrace();
        }
```
![부하주기](https://user-images.githubusercontent.com/84304041/124995369-3ab14b00-e082-11eb-8822-d1580810bf53.PNG)

1. siege로 부하 . 서킷브레이커로 차단발생
``` 
kubectl exec -it siege -- /bin/bash
# 42명 사용자가 100초 동안 진입
siege -c20 -t100S -r5 -v --content-type "application/json" 'http://delivery:8080/deliveries POST {"status":"CANCLED"}' -v
```
![서킷브레이커1](https://user-images.githubusercontent.com/84304041/124995086-c70f3e00-e081-11eb-8356-d43970ae8616.PNG)
![서킷브레이커3](https://user-images.githubusercontent.com/84304041/124995197-f4f48280-e081-11eb-948c-af0eada7f1a7.PNG)


- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 

### Autoscale (HorizontalPodAutoscaler)
1. metrics 설치
``` 
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.3.7/components.yaml
kubectl get deployment metrics-server -n kube-system
``` 
2. 리소스 설정

[deployment.yml]

![HPA](https://user-images.githubusercontent.com/84304041/124995544-7ea45000-e082-11eb-849b-c0027c15a254.PNG)

- kubectl get deploy delivery -o yaml  로 설정 재확인 
![hpa4](https://user-images.githubusercontent.com/84304041/124995864-f7a3a780-e082-11eb-92a2-5dfad893c1ab.PNG)


3. 명령어로 HPA 설정
```
# CPU 사용량이 20%를 넘으면 replica를 3개까지 늘림
kubectl autoscale deployment order --cpu-percent=20 --min=1 --max=3
```
![hpa5](https://user-images.githubusercontent.com/84304041/124995892-02f6d300-e083-11eb-8171-f3f6ef0df72a.PNG)

5. 
 siege로 부하 설정

![hpa6](https://user-images.githubusercontent.com/84304041/124995935-1144ef00-e083-11eb-801e-6cfd09111a07.PNG)

6. 부하가 발생하고, replica가  늘어난 것 확인
![hpa7](https://user-images.githubusercontent.com/84304041/124995989-2c176380-e083-11eb-88d1-e7855d399ba9.PNG)

- siege 부하가 마무리되면서 delivery pod 는 다시 1개로 줌
- ![hpa8](https://user-images.githubusercontent.com/84304041/124996124-66810080-e083-11eb-84fb-3ed75edbaaa6.PNG)



### Zero-downtime deploy (Readiness Probe)
1. 먼저 Readiness를 주석처리 후 제대로 적용되었는지 명령어로 확인

![무정지배포1](https://user-images.githubusercontent.com/84304041/124996789-85cc5d80-e084-11eb-8c51-59092ca1bd89.PNG)

2 siege로 접속하여 부하를 넣고, 신규버전으로 다시 update 진행 시 서비스 중단 확인


![무정지배포2](https://user-images.githubusercontent.com/84304041/124997098-0a1ee080-e085-11eb-8450-07c4fc4dea14.PNG)

![무정지배포4](https://user-images.githubusercontent.com/84304041/124997199-2ae73600-e085-11eb-824f-3330d2595ca3.PNG)

![무정지배포4 5](https://user-images.githubusercontent.com/84304041/124997058-f8d5d400-e084-11eb-8c89-22eb14d5c8bd.PNG)


3. 다시 deployment.yml 파일에서 Readiness 주석처리한 부분 제외하여 apply 처리함
![무정지배포5](https://user-images.githubusercontent.com/84304041/124997257-42beba00-e085-11eb-9ff4-bd01535e6cb6.PNG)


4. 다시 siege로 들어와서 부하발생 한 뒤 신규버전으로 update 진행 시 서비스 무중단으로 업무처리 됨을 확인
![무정지배포6](https://user-images.githubusercontent.com/84304041/124997302-52d69980-e085-11eb-8f2c-eff9ba2b36e0.PNG)
![무정지배포8](https://user-images.githubusercontent.com/84304041/124997327-5cf89800-e085-11eb-957a-e1f6fd0c1877.PNG)
![무정지배포9](https://user-images.githubusercontent.com/84304041/124997336-61bd4c00-e085-11eb-8dc4-24cbc43ca253.PNG)

### Self-healing (Liveness Probe)
1. yml 파일에 Liveness 적용
![셀프힐링1](https://user-images.githubusercontent.com/84304041/124997473-a1843380-e085-11eb-8452-118705814348.PNG)


2. Pod가 에러로 종료가 될 때까지 siege로 계속해서 부하 발생
- 300명이 100초간 부하

![셀프힐링3](https://user-images.githubusercontent.com/84304041/124997583-dabca380-e085-11eb-9608-ae66f446e85e.PNG)

![셀프힐링4](https://user-images.githubusercontent.com/84304041/124997626-f32cbe00-e085-11eb-8208-7a1c575fdac2.PNG)

5. kubectl get po -w로 모니터링하면서 delivery pod가  RESTART로 재기동이 발생하는 모습 확인
6. 
![셀프힐링5](https://user-images.githubusercontent.com/84304041/124997713-16576d80-e086-11eb-8d50-24df577b8794.PNG)



### Polyglot

cook 서비스는 hsqldb를 사용하여 정상작동 확인했다
![폴릭드랏쿡](https://user-images.githubusercontent.com/84304041/124999061-66cfca80-e088-11eb-97b3-319b72dde9a2.PNG)

요리서비스가 정상적으로 post되고 get되었다. 

![폴리그랏쿡2](https://user-images.githubusercontent.com/84304041/124999089-6fc09c00-e088-11eb-9215-596c8b789fa7.PNG)
![폴리그랏쿡3](https://user-images.githubusercontent.com/84304041/124999098-72bb8c80-e088-11eb-9042-10e16df7a840.PNG)


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

