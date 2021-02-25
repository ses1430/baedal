# 개인과제 : 배달 서비스 (baedal)

# 서비스 시나리오

기능적 요구사항
1. 고객이 메뉴를 선택하여 주문과 동시에 결제한다.
3. 결제가 완료되면 배송이 요청된다.
4. 배송이 요청되면 즉시 배송이 시작된다.
5. 배달원이 배달하면 배달이 완료된다.
6. 고객은 주문을 취소할 수 있다.
7. 주문이 취소되면 결제를 취소한다.
8. 결제가 취소되면 배송을 취소한다.
9. 고객이 주문상태를 중간중간 조회한다.

비기능적 요구사항
1. 트랜잭션
    1. 결제가 취소되지 않으면 주문취소도 되지 않는다. 
1. 장애격리
    1. 배달 기능이 수행되지 않더라도 결제는 365일 24시간 받을 수 있어야 한다.
    2. 결제 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다.

# 분석/설계

## Event Storming 결과
![캡처](https://user-images.githubusercontent.com/452079/109057315-efc31680-7724-11eb-85f7-78cedbf4dd10.PNG)

## 헥사고날 아키텍처 다이어그램 도출
![hexagonal](https://user-images.githubusercontent.com/452079/109100297-f7f07580-7767-11eb-9170-c28db8f6eaa4.PNG)

### 비기능 요구사항에 대한 검증

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
        - 주문취소시 배달취소에 대해서는 Request-Response 방식 처리
        - 주문시 결제요청, 결제완료시 배달요청 되는것에 있어서
          order/payment/delivery 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.

# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 
구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)


## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다.

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록, 
  데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

- 적용 후 REST API 의 테스트

```
# order 서비스의 주문처리
http http://a497f79f966814b10ac57259e6fce4ea-1896896990.ap-northeast-2.elb.amazonaws.com:8080/orders menuId=1 menuNm=Gimbab qty=1
http http://a497f79f966814b10ac57259e6fce4ea-1896896990.ap-northeast-2.elb.amazonaws.com:8080/orders menuId=2 menuNm=Juice qty=1

# delivery 서비스의 배달처리
http PATCH http://a497f79f966814b10ac57259e6fce4ea-1896896990.ap-northeast-2.elb.amazonaws.com:8080/deliveries/2 status=complete

# order 서비스의 취소처리
http PATCH http://a497f79f966814b10ac57259e6fce4ea-1896896990.ap-northeast-2.elb.amazonaws.com:8080/orders/1 status=cancel

# 주문 및 배달상태 확인
http://a497f79f966814b10ac57259e6fce4ea-1896896990.ap-northeast-2.elb.amazonaws.com:8080/orders
http://a497f79f966814b10ac57259e6fce4ea-1896896990.ap-northeast-2.elb.amazonaws.com:8080/deliveries

{
  "_embedded" : {
    "orders" : [ {
      "menuId" : 1,
      "menuNm" : "Juice",
      "qty" : 1,
      "status" : "cancel",
      "deliveryStatus" : "cancelled",
      "deliveryId" : 1,
      "_links" : {
        "self" : {
          "href" : "http://order:8080/orders/1"
        },
        "order" : {
          "href" : "http://order:8080/orders/1"
        }
      }
    }, {
      "menuId" : 2,
      "menuNm" : "Gimbab",
      "qty" : 2,
      "status" : "ordered",
      "deliveryStatus" : "complete",
      "deliveryId" : 2,
      "_links" : {
        "self" : {
          "href" : "http://order:8080/orders/2"
        },
        "order" : {
          "href" : "http://order:8080/orders/2"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://order:8080/orders{?page,size,sort}",
      "templated" : true
    },
    "profile" : {
      "href" : "http://order:8080/profile/orders"
    }
  },
  "page" : {
    "size" : 20,
    "totalElements" : 2,
    "totalPages" : 1,
    "number" : 0
  }
}

{
  "_embedded" : {
    "deliveries" : [ {
      "orderId" : 1,
      "status" : "cancelled",
      "_links" : {
        "self" : {
          "href" : "http://delivery:8080/deliveries/1"
        },
        "delivery" : {
          "href" : "http://delivery:8080/deliveries/1"
        }
      }
    }, {
      "orderId" : 2,
      "status" : "complete",
      "_links" : {
        "self" : {
          "href" : "http://delivery:8080/deliveries/2"
        },
        "delivery" : {
          "href" : "http://delivery:8080/deliveries/2"
        }
      }
    } ]
  },
  "_links" : {
    "self" : {
      "href" : "http://delivery:8080/deliveries{?page,size,sort}",
      "templated" : true
    },
    "profile" : {
      "href" : "http://delivery:8080/profile/deliveries"
    }
  },
  "page" : {
    "size" : 20,
    "totalElements" : 2,
    "totalPages" : 1,
    "number" : 0
  }
}
```

## CQRS

mypage를 구현하여 order, menu, delivery 서비스의 데이터를 DB Join없이 조회할 수 있다.
```
```

## Gateway

gateway 서비스를 통하여 동일 진입점으로 진입하여 각 마이크로 서비스를 접근할 수 있다.

![gateway](https://user-images.githubusercontent.com/452079/109101422-ffb11980-7769-11eb-9e01-a47063e86d67.PNG)

외부에서 접근을 위하여 Gateway의 Service는 LoadBalancer Type으로 생성했다.

![gateway_svc](https://user-images.githubusercontent.com/452079/109101517-30914e80-776a-11eb-81fb-7e5258ac49e6.PNG)

## Deploy

CodeBuild를 사용하지 않고 docker images를 AWS를 통해 수작업으로 배포/기동하였음.

```
# AWS 컨테이너 레지스트리에 이미지 리파지토리 생성
aws ecr create-repository --repository-name skcc15-order    --image-scanning-configuration scanOnPush=true --region ap-southeast-1

# package & docker image build/push
mvn package
docker build -t 496278789073.dkr.ecr.ap-southeast-1.amazonaws.com/skcc15-order:v3 .
docker push 496278789073.dkr.ecr.ap-southeast-1.amazonaws.com/skcc15-order:v3

# docker 이미지로 Deployment 생성
kubectl create deploy order --image=496278789073.dkr.ecr.ap-southeast-1.amazonaws.com/skcc15-order:v3

# expose
kubectl expose deploy order --type=ClusterIP --port=8080
```

## Autoscale (HPA)

결제요청이 일시적으로 급증하는 경우를 대비하여 autoscale(HPA)를 적용함.

```
# payment 서비스에 대해 cpu 부하가 15%를 넘으면 10개까지 scale out 설정
kubectl autoscale deploy payment --min=1 --max=10 --cpu-percent=15

# 부하를 주기 위해 siege
siege -c20 -t30S -v --content-type "application/json" 'http://af9c68783609a42e0b7512ce75a0426f-1837115883.ap-southeast-1.elb.amazonaws.com:8080/payments POST {"orderId":"100", "status":"paid"}'
```

- metric server를 통해 수집된 부하가 threshold인 15% 넘어서 10개까지 auto scaleout 됨

![hpa_scaleout](https://user-images.githubusercontent.com/452079/109101986-2a4fa200-776b-11eb-9011-224ca4f6fb04.png)

- siege 결과도 100% 가용

![hpa_scaleout_siege](https://user-images.githubusercontent.com/452079/109102052-5a974080-776b-11eb-87fe-3ce10b79ade1.png)


## ConfigMap

초기 주문시 상태값에 해당하는 텍스트를 ConfigMap으로 처리함.

- configmap.yml 파일 내용 : key "text"에 해당하는 값을 "ordered"로 설정

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: order-cm
data:
  text: ordered
```

- deployment.yaml 파일에 env 설정

```
      containers:
        - name: order
          image: 496278789073.dkr.ecr.ap-southeast-1.amazonaws.com/skcc15-order:v3
          ports:
            - containerPort: 8080
          env:
            - name: TEXT
              valueFrom:
                configMapKeyRef:
                  name: order-cm
                  key: text
```

- 설정한 order pod 내에서 환경변수로 설정된 것을 확인

```
root@labs-372165582:/home/project/personal/baedal/payment/kubernetes# kubectl exec -it order-f6cc85dd8-vhdmx -- /bin/sh
/ # env | grep TEXT
TEXT=ordered
/ #
```

- java코드내에서 환경변수 획득하여 order status 초기값으로 세팅

```
    @PrePersist
    public void onPrePersist() {
        System.out.println("########## Configmap TEXT => " + System.getenv("TEXT"));
        this.setStatus(System.getenv("TEXT"));
    }
```

## Polyglot (Persistence)

payment 서비스의 DB를 기존 h2 가 아닌 hsqldb로 구성하기 위해, maven dependancy를 추가함.

```
# payment 서비스의 pom.xml

    <dependency>
        <groupId>org.hsqldb</groupId>
        <artifactId>hsqldb</artifactId>
        <version>2.5.1</version>
        <scope>runtime</scope>
    </dependency>
```

## Liveness / Readiness 설정
order 서비스 deployment.xml 에 Liveness, Readiness를 httpGet 방식으로 설정함
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order
  labels:
    app: order
spec:
  replicas: 1
  selector:
    matchLabels:
      app: order
  template:
    metadata:
      labels:
        app: order
    spec:
      containers:
        - name: order
          image: 496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/team05-order:v1
          ports:
          - containerPort: 8080
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5

```

## Self-Healing

order pod를 강제종료처리시, liveness에 의해 자동으로 다른 pod가 생성되는 모습
```
kubectl delete pod order-cb5d6b495-knfrp
```
![image](https://user-images.githubusercontent.com/452079/108833009-974e2500-760f-11eb-99c2-0e4b127f245d.png)

생성된 order pod의 상세정보
```
Containers:
  order:
    Container ID:   docker://0cafff76bed772615cfe8b74845e50f3979fec7fe5da5a0587b7ae2b4fe79b4e
    Image:          496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/team05-order:v1
    Image ID:       docker-pullable://496278789073.dkr.ecr.ap-northeast-2.amazonaws.com/team05-order@sha256:8b09186218c6b2517e8829cdf48decfd105d10831c2870677b29f50adfcbc61a
    Port:           8080/TCP
    Host Port:      0/TCP
    State:          Running
      Started:      Tue, 23 Feb 2021 19:38:50 +0900
    Ready:          True
    Restart Count:  0
    Liveness:       http-get http://:8080/actuator/health delay=120s timeout=2s period=5s #success=1 #failure=5
    Readiness:      http-get http://:8080/actuator/health delay=10s timeout=2s period=5s #success=1 #failure=10
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from default-token-c55nd (ro)
Conditions:
  Type              Status
  Initialized       True 
  Ready             True 
  ContainersReady   True 
  PodScheduled      True
```
