app.controller("ShowDeployController", function($scope, $http) {

    $scope.deploys;

    /**
     * 查看正在运行的项目
     */
    $scope.showDeploy = function (){
        $http({
            method: 'get',
            url: '/deployFile'
        }).then(function (response) {
            var result = response.data;
            $scope.deploys = result.data;
        });
    }

    /**
     * 停止项目
     */
    $scope.stop = function (name){
        if(confirm("确认停止项目！")){
            $http({
                method: 'get',
                url: '/stop?fileName=' + name
            }).then(function (response) {
                if(response.data.success) {
                    $scope.deploys[name] = false;
                }else {
                    alert(name + ":::" + response.data.note);
                }
            });
        }
    }

    /**
     * 重启项目
     */
    $scope.restart = function (name){
        if(confirm("确认重启项目！")) {
            $http({
                method: 'get',
                url: '/restart?fileName=' + name
            }).then(function (response) {
                if (response.data.success){
                    $scope.deploys[name] = true;
                }else {
                    alert(name + ":::" + response.data.note);
                }
            });
        }
    }

    $scope.showDeploy();
});