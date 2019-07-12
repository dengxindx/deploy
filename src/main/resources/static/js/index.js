var app = angular.module("app", ["ngRoute"])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/deploy', {
                templateUrl: '/views/deploy-template.html',
                controller: 'DeployController'
            })
            .when('/showDeploy', {
                templateUrl: '/views/show-deploy-template.html',
                controller: 'ShowDeployController'
            });
    })