package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class AwsServiceStack extends Stack {
    public AwsServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public AwsServiceStack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        Map<String, String> authentication = new HashMap<>();
        authentication.put("SPRING_DATASOURCE_URL","jdbc:mysql://" + Fn.importValue("pedidos-db-endpoint") +
                ":3306/alurafood-pedidos?createDatabaseIfNotExist=true");
        authentication.put("SPRING_DATASOURCE_USERNAME","admin");
        authentication.put("SPRING_DATASOURCE_PASSWORD",Fn.importValue("pedidos-db-senha"));

        IRepository repositorio = Repository.fromRepositoryName(this, "repositorio", "img-pedidos-ms");

        ApplicationLoadBalancedFargateService awsFargateService = ApplicationLoadBalancedFargateService.Builder.create(this, "AwsFargateService")
                .serviceName("estudo-service-ola")
                .cluster(cluster)           // Required
                .cpu(512)                   // Default is 256
                .desiredCount(1)            // Default is 1
                .listenerPort(8080)
                .assignPublicIp(true)
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromEcrRepository(repositorio))
                                .containerPort(8080)
                                .environment(authentication)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .logGroup(LogGroup.Builder.create(this, "PedidosMsLogGroup")
                                                .logGroupName("PedidosMsLog")
                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                .build())
                                        .streamPrefix("PedidosMS")
                                        .build()))
                                .containerName("app_ola")
                                .build())
                .memoryLimitMiB(1024)       // Default is 512
                .publicLoadBalancer(true)   // Default is true
                .build();

        ScalableTaskCount scalableTarget = awsFargateService.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(1)
                .maxCapacity(3)
                .build());
        scalableTarget.scaleOnCpuUtilization("CpuScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(70)
                        .scaleInCooldown(Duration.minutes(3))
                        .scaleOutCooldown(Duration.minutes(2))
                .build());
        scalableTarget.scaleOnMemoryUtilization("MemoryScaling", MemoryUtilizationScalingProps.builder()
                .targetUtilizationPercent(65)
                .scaleInCooldown(Duration.minutes(3))
                .scaleOutCooldown(Duration.minutes(2))
                .build());

        awsFargateService.getTargetGroup().configureHealthCheck(HealthCheck.builder().port("8080").path("/pedidos").interval(Duration.seconds(30)) // Intervalo entre verificações
                .healthyHttpCodes("200-499")   // Códigos HTTP que indicam que o serviço está saudável
                .unhealthyThresholdCount(2).build());

    }
}
