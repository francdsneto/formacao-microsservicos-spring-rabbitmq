package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class AwsInfraApp {
    public static void main(final String[] args) {
        App app = new App();
        AwsVpcStack vpcStack = new AwsVpcStack(app, "Vpc");
        AwsClusterStack clusterStack = new AwsClusterStack(app, "Cluster", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        AwsRDSStack rdsStack = new AwsRDSStack(app, "Rds", vpcStack.getVpc());
        rdsStack.addDependency(vpcStack);

        AwsServiceStack awsServiceStack = new AwsServiceStack(app, "Service", clusterStack.getCluster());
        awsServiceStack.addDependency(clusterStack);
        awsServiceStack.addDependency(rdsStack);

        app.synth();
    }
}

