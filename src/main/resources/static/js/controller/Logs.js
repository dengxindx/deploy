app.controller("LogsController", function($scope, $http) {

    /**
     * 展示的最大页数
     * @type {number}
     */
    $scope.showPageNum = 10;

    /**
     * 日志展示的内容
     * @type {string}
     */
    $scope.fileContext = "";

    /**
     * 页数(最多显示十页)
     * @type {Array}
     */
    $scope.pageCount = [];

    /**
     * 部署项目的jar包名
     */
    $scope.jarName

    /**
     * 当前页
     */
    $scope.currentPage

    /**
     * 某个部署jar包对应的日志文件列表
     */
    $scope.filelogs

    /**
     * 记录某个部署jar包对应的当前操作的日志文件名
     */
    $scope.ongLog

    /**
     * 该日志文件总包含的页数
     */
    $scope.totalPage

    /**
     * 查看正在运行的项目
     */
    $scope.showDeploysLogs = function (){
        $scope.logsClick = true;
        $http({
            method: 'get',
            url: '/deployLogs'
        }).then(function (response) {
            var result = response.data;
            $scope.deploysLogs = result.data;
        });
    }

    /**
     * 查看所选项目日志
     */
    $scope.logs = function (name){
        $scope.jarName = name;
        $scope.logsClick = false;
        $scope.filelog = true;
        $scope.showFilelog = true;
        $http({
            method: 'get',
            url: '/logs?name=' +  name
        }).then(function (response) {
            var result = response.data;
            $scope.filelogs = result.data;
        });
    }

    /**
     * 查看所选日志
     * name : jar包的日志文件名
     */
    $scope.showLog = function (name, pageCount){
        $scope.ongLog = name;
        $scope.pageCount = [];
        $scope.filelog = false;
        $scope.showFilelog = false;
        /**
         * 设定当前页
         * @type {number}
         */
        $scope.currentPage = pageCount;
        $http({
            method: 'get',
            url: '/showLog?name=' +  name + "&pageCount=" + pageCount + "&jarName=" + $scope.jarName
        }).then(function (response) {
            var result = response.data;
            $scope.totalPage = result.data.totalPage;
            for (var i = 1; i <= (result.data.totalPage <= $scope.showPageNum ? result.data.totalPage : $scope.showPageNum); i++) {
                $scope.pageCount.push(i);
            }
            $scope.fileContext = result.data.content;
        });
    }

    /**
     * 跳转到某页
     * x : 指定页
     */
    $scope.clickPage = function (x){
        if ($scope.currentPage == x){
            return;
        }
        $scope.fileContext = "";
        $scope.currentPage = x;
        $http({
            method: 'get',
            url: '/showLog?name=' +  $scope.ongLog + "&pageCount=" + x + "&jarName=" + $scope.jarName
        }).then(function (response) {
            var result = response.data;
            $scope.fileContext = result.data.content;
        });
    }

    $scope.firstPage = function (){
        if ($scope.currentPage == 1){
            return;
        }
        $scope.showLog($scope.ongLog, 1);
    }

    $scope.prev = function (){
        if ($scope.currentPage == 1){
            return;
        }
        if ($scope.pageCount.indexOf($scope.currentPage - 1) == -1){
            for (var i = 0; i < $scope.pageCount.length; i++) {
                $scope.pageCount[i] += -1;
            }
        }
        $scope.clickPage($scope.currentPage - 1);
    }

    $scope.next = function (){
        if ($scope.currentPage == $scope.totalPage){
            return;
        }

        if ($scope.pageCount.indexOf($scope.currentPage + 1) == -1){
            for (var i = 0; i < $scope.pageCount.length; i++) {
                $scope.pageCount[i] += 1;
            }
        }
        $scope.clickPage($scope.currentPage + 1);
    }

    $scope.lastPage = function (){
        if ($scope.currentPage == $scope.totalPage){
            return;
        }
        if ($scope.totalPage <= $scope.showPageNum){
            $scope.clickPage($scope.totalPage);
        }else {
            $scope.pageCount = [];
            $scope.currentPage = $scope.totalPage;
            $http({
                method: 'get',
                url: '/showLog?name=' +  $scope.ongLog + "&pageCount=" + $scope.totalPage + "&jarName=" + $scope.jarName
            }).then(function (response) {
                var result = response.data;
                $scope.totalPage = result.data.totalPage;
                for (var i = ($scope.totalPage - ($scope.showPageNum - 1)); i <= result.data.totalPage; i++) {
                    $scope.pageCount.push(i);
                }
                $scope.fileContext = result.data.content;
            });
        }
    }

    $scope.showBack = function (){
        $scope.fileContext = "";
        $scope.filelog = true;
        $scope.showFilelog = true;
    }

    $scope.showDeployBack = function (){
        $scope.logsClick = true;
    }

    $scope.showDeploysLogs();
});