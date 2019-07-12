app.controller("DeployController", function($scope, $http) {
    $scope.deploying = false;

    // 上传部署zip包
    $scope.deploy = function (){
        $scope.deploying = true;
        $("#uploadBtn").attr("disabled", "disabled");
        $scope.st = "";
        var formData = new FormData();
        formData.append('file', document.querySelector('input[type=file]').files[0]);
        formData.append('cmd', $scope.cmd);
        // formData.append(${_csrf.parameterName, ${_csrf.token});

        $http({
            method: 'post',
            url: '/deploy',
            data: formData,
            headers: {'Content-Type': undefined},
            transformRequest: angular.identity
        }).then(function (response) {
            $("#uploadBtn").removeAttr("disabled");
            $scope.deploying = false;
            // 获取zip包的上传后状态
            // console.log(response.data.note);
            // console.log(response.data.code);
            // console.log(response.data.success);
            // 启动结果显示
            $scope.st = response.data;
        });
    }
});