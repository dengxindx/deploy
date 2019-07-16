var app = angular.module("app", ["ngRoute"])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/deploy', {
                templateUrl: '/views/deploy-template.html',
                controller: 'DeployController'
            })
            .when('/logs', {
                templateUrl: '/views/logs-template.html',
                controller: 'LogsController'
            })
            .when('/showDeploy', {
                templateUrl: '/views/show-deploy-template.html',
                controller: 'ShowDeployController'
            });
    })