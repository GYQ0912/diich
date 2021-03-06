var cutStr = {
    get: function (str) {//计算长度 汉字是2
        var real = 0;
        var len = str.length;
        var charCode = -1;
        for (var i = 0; i < len; i++) {
            charCode = str.charCodeAt(i);
            if (charCode >= 0 && charCode <= 128) {
                real += 1;
            } else {
                real += 2;
            }
        }
        return real;
    },
    setStage: function (str) {
        var len = cutStr.get(str) / 2;
        if (len >= 200 && len <= 1200) {
            var strLi = '<div class="plain_text"><ul><li><p>' + str.substring(0, 400) + '</p></li><li><p>' + str.substring(400, 800) + '</p></li><li style="margin-right: 0"><p>' + str.substring(800, 1200) + '</p></li></ul></div>';
            return strLi;
        }
    }
};



//详情页相关
var Detail = { //详情页用到的效果
    productsTab: function () { //作品分页
        var _products = $('.product_list');
        _products.each(function () {
            //列表相关属性
            var _ul = _products.find('ul'); //获取列表ul
            var _li = _ul.find('li'); //获取列表li
            var liLen = _li.length; //获取列表的length

            //分页相关属性
            var _page = _products.find('.page'); //分页显示容器
            var currentNum = 4; //当前页显示个数
            var total = Math.ceil(liLen / currentNum); //分页总数

            //创建分页数码
            if (total > 1) {
                for (var i = 1; i <= total; i++) {
                    _page.append('<span>0' + i + '</span>');
                }
                var _pageSpan = _page.find('span');
                _pageSpan.eq(0).addClass('active');

                //计算列表滚动
                _pageSpan.on('click', function () {
                    var index = $(this).index();
                    _ul.animate({'margin-left': -index * 1170 + 'px'}, 300);
                    $(this).addClass('active').siblings('span').removeClass('active');
                });
            }
        });
    },
    cutText: function () { //截取长文本 大于3000
        var lgText = $('div[data-type=lgText]');
        var textMore = $('.text_more');
        var _span = textMore.find('span');
        var oldH = lgText.height();
        var newH = 600; //基本高度
        if (lgText.height() >= newH) {
            lgText.animate({'height': newH + 'px'}, 0);
        }
        _span.eq(1).hide();
        _span.on('click', function () {
            var _this = $(this);
            if (_this.hasClass('show')) {
                lgText.animate({'height': oldH + 'px'}, 100);
                setTimeout(function () {
                    _this.hide().siblings('span').show();
                }, 100);
            } else {
                lgText.animate({'height': newH + 'px'}, 100);
                setTimeout(function () {
                    _this.hide().siblings('span').show();
                }, 100);
            }
        })

    },
    scrollFloor: function () { //楼层
        var parent = $('.side_fixed');
        var _ul = parent.find('ul'); //导航
        var _floor = $('section.floor'); //楼层
        var _nav = $('.card header h4'); //楼层标题
        var arr = []; //把pros对应的几个位置标示出来
        //获取所有楼层标题
        _nav.each(function (i) { //给右侧悬浮添加标题
            _ul.append('<li><span>' + $(this).text() + '</span><strong>0' + (i + 1) + '</strong></li>');
        });
        var _li = _ul.find('li');
        //滚动
        _floor.each(function () { //标记所有楼层导航的高度
            var offsettop = $(this).offset().top-170;
            arr.push(parseInt(offsettop)); //火狐有半个像素的情况，故取整
        });
        var firstFloor = arr[0];
        //滚动鼠标
        $(window).scroll(function () {
            var d = $(document).scrollTop();
            if (d >= firstFloor) {
                parent.fadeIn('fast');
                for (var i = 0; i < arr.length; i++) {
                    if (d < arr[i]) {
                        break;
                    }
                }
                _li.eq(0).addClass('active').siblings('li').removeClass('active');
                if (i > 0) {_li.eq(i - 1).addClass('active').siblings('li').removeClass('active');}
                if (i == arr.length) {i--;}
            } else {
                parent.fadeOut('fast');
            }
        });

        //点击回到当前楼层
        _ul.on('click', 'li', function () {
            var _index = $(this).index();
            var _top = _floor.eq(_index).offset().top;
            $(this).addClass('active').siblings('li').removeClass('active');
            $('html,body').stop(true).animate({'scrollTop': _top + 'px'}, 500);
        });
    },
    modal:{//详情弹框
        init:function () {
            this.show();
            this.computeSize();
        },
        computeSize:function () {
            var el=$('.card_main .floor a.album');
            var imgSize=jsonAll.imgs.length;
            var videoSize=jsonAll.videos.length;

            if(imgSize==0 && videoSize==0){
                el.remove();
            }

            //图片数量大于0 视频数量为0
            if(imgSize > 0 && videoSize == 0){
                el.html('<i class="icon_img"></i>'+imgSize+'张图片');
            }
            //视频数量大于0 图片数量为0
            if(videoSize > 0 && imgSize==0){
                el.html('<i class="play_sm"></i>&nbsp;&nbsp;'+videoSize+'个视频');
            }

            //
            if(imgSize > 0 && videoSize > 0){//(2个视频／9张图片)
                el.html('<i class="play_sm"></i>&nbsp;&nbsp;('+videoSize+'个视频/'+imgSize+'张图片)');
            }
        },
        filter:function () {//判断页面添加基本属性
            var body=$('body');
            var isPage=body.is('.js-project');
            var _data={};

            //判断中英文
            if(jsonHead.lang=='chi'){
                if(isPage){
                    _data.unesco='UNESCO 认证项目';
                }else {
                    _data.unesco='UNESCO 认证传承人';
                }
                _data.tabs='<span>图片</span><span>视频</span>';
            }else{
                if(isPage){
                    _data.unesco='UNESCO certification of non-legacy projects';
                }else {
                    _data.unesco='UNESCO certification of non-legacy masters';
                }
                _data.tabs='<span>Picture</span><span>Video</span>';
            }

            //判断页面
            if(isPage){
                _data.baseUrl='http://resource.efeiyi.com/image/project/';
                _data.name=jsonHead.projectName;
                _data.page='project';
            }else{
                _data.baseUrl='http://resource.efeiyi.com/image/master/';
                _data.name=jsonHead.masterName;
                _data.page='master';
            }
            _data.headImage=jsonHead.headImage[0].uri;
            return _data;
        },
        formatData:function (type) {//type 传入的是 jsonAll/json   id为区域contentFragmentId
            var data=null;
            var _data={};
            if(type==='all'){//全部数据
                data=jsonAll;
                //判断图片和视频的数量
                _data.imgSize=data.imgs.length ? data.imgs.length : 0;
                _data.videoSize=data.videos.length ? data.videos.length : 0;
                _data.imgs=jsonAll.imgs;
                _data.videos=jsonAll.videos;
            }else {//区域数据
                data=json;
                var _index=0;
                //找出当前区域的数据
                for (var i in data){
                    if(data[i].contentFragmentId==type){
                        _index=i;
                    }
                }
                
                _data.imgs=data[_index].imgs;
                _data.videos=data[_index].videos;
                //判断图片和视频的数量
                _data.imgSize=_data.imgs.length;
                _data.videoSize=_data.videos.length;
            }
			
			

            return _data;
        },
        createDom:function () {//创建元素
            var _data=this.filter();

            function headDom(type) {
                var str='<div class="head">'+
                    '<div class="menu">'+ _data.tabs +'</div>'+
                    '<a href="" class="icon_close"></a>'+
                    '</div>';
                return str;
            }
            function titleDom(type) {
                var str='<div class="title"><span class="dt"></span>'+
                    '<div class="master">'+
                    '<div class="item">'+
                    '<a class="avatar"><img src="'+ _data.baseUrl + _data.headImage +'?x-oss-process=style/head-image-style" alt="" width="94" height="70"></a>'+
                    '<span class="name">'+ _data.name +'</span><span class="auth">'+_data.unesco+'</span></div>'+
                    '</div>'+
                    '</div>';
                return str;
            }
            function albumDom(type) {
                var str='<div class="items album" style="display: block;">'+
                    titleDom()+
                    '<div class="main">'+
                    '<ul class="media">'+
                    '<li><a href=""><img src="" alt="" data-type="0" data-id="1"></a></li>'+
                    '</ul>'+
                    '<ul class="num">'+
                    '<li class="active a-active">01</li>'+
                    '<li class="line">/</li>'+
                    '<li class="total"></li>'+
                    '</ul>'+
                    '</div>'+
                    '<span class="prev" style="display: block;"></span>'+
                    '<span class="next" style="display: block;"></span>'+
                    '</div>';
                return str;
            }
            function videoDom(type) {
                var str='<div class="items video">'+
                    titleDom()+
                    '<div class="main">'+
                    '<ul class="media"></ul>'+
                    '<ul class="num"></ul>'+
                    '</div>'+
                    '<span class="prev" style="display: block;"></span>'+
                    '<span class="next" style="display: block;"></span>'+
                    '</div>';

                return str;
            }

            var mainDom='<div class="media_layer"><div class="content">'+ headDom() + albumDom() + videoDom() +'</div></div>'
            return mainDom;
        },
        show:function () {
            var _this=this;
            //全部
            $('.card_main .floor a.album').on('click',function () {
                var type=$(this).attr('data-id');
                $('body').append(_this.createDom());
                _this.handleDom(type);
            });

            //区域
            $('.card .text_img .media .more a').on('click',function () {
                var type=$(this).attr('data-id');
				console.log(type)
                $('body').append(_this.createDom());
                _this.handleDom(type);
                return false;
            });
        },
        handleDom:function (type) {//交互效果
            var _base=this.filter();  //公共的数据
            var _data=this.formatData(type); //具体的图片和视频数据

            var obj = $('.media_layer');
            var head = obj.find('.head');
            var items = obj.find('.items');
            var album = obj.find('.album');
            var albumLi = album.find('.media li');
            var video = obj.find('.video');
            var videoMedia = album.find('.media');
            var close = obj.find('.icon_close');
            var cur=0;
            var albumCur=0;
            var videoCur=0;

            //初始化
            (function () {
                var _span=head.find('span');
                if(_data.imgSize!=0 || _data.videoSize!=0){
                    _span.eq(0).addClass('active');
                }
                if(_data.imgSize===0){
                    _span.eq(0).hide();
                    _span.eq(1).addClass('active');
                }
                if(_data.videoSize===0){
                    _span.eq(0).addClass('active');
                    _span.eq(1).remove();
                }

                _span.on('click',function () {
                    $(this).addClass('active').siblings('span').removeClass('active');
                    items.eq($(this).index()).show().siblings('.items').hide();
                    mediaPause();
                });

            })();

            //相册
            albumHandle();
            function albumHandle() {
                var albumTitle = album.find('.title');
                var albumNum = album.find('.num');
                var prev=album.find('.prev');
                var next=album.find('.next');

                //上一页
                prev.on('click',function () {
                    next.removeClass('active');
                    if(albumCur > 0){albumCur--;}
                    if(albumCur==0){$(this).addClass('active');}
                    bindData(albumCur);
                });

                //下一页
                next.on('click',function () {
                    prev.removeClass('active');
                    if(albumCur < _data.imgSize-1){albumCur++;}
                    if(albumCur==_data.imgSize-1){$(this).addClass('active');}
                    bindData(albumCur);
                });

                bindData(0);
                //绑定数据
                function bindData(index) {
                    var _imgs=_data.imgs[index];


                    if(_imgs.description){
                        albumTitle.find('.dt').text(_imgs.description);
                    }else{
                        albumTitle.find('.dt').text('');
                    }

                    albumLi.html('<img src="'+ _base.baseUrl + _imgs.uri +'" />');
                    albumNum.find('.active').text(common.pad(albumCur+1));
                    albumNum.find('.total').text(common.pad(_data.imgSize));
                }

            }

            //视频
            if(_data.videoSize != 0){
                videoHandle();
            }

            function videoHandle() {
                var videoTitle = video.find('.title');
                var media = video.find('.media');
                var prev=video.find('.prev');
                var next=video.find('.next');


                bindData(cur);
                var liStr='';
                for(var i=0;i<_data.videos.length;i++){
                    liStr+='<li style="display: none;"><video controls src="' + _data.videos[i].uri +'"></video></li>';
                }
                media.html(liStr).find('li').eq(0).show();


                //上一页
                prev.on('click',function () {
                    next.removeClass('active');
                    if(videoCur > 0){videoCur--;}
                    if(videoCur==0){$(this).addClass('active');}
                    media.find('li').eq(videoCur).show().siblings('li').hide();
                    bindData(videoCur);
                    mediaPause();
                });

                //下一页
                next.on('click',function () {
                    prev.removeClass('active');
                    if(videoCur < _data.videoSize-1){videoCur++;}
                    if(videoCur==_data.videoSize-1){$(this).addClass('active');}
                    media.find('li').eq(videoCur).show().siblings('li').hide();
                    bindData(videoCur);
                    mediaPause();
                });

                //绑定数据
                function bindData(index) {
                    var _video=_data.videos[index];
                    if(_video.description){
                        videoTitle.find('.dt').text(video.description);
                    }else {
                        videoTitle.find('.dt').text('');
                    }
                }

            }

            //暂停所有视频
            function mediaPause(){
                video.find('.media li').each(function () {
                    $(this).find('video').get(0).pause();
                });
            }

            //关闭
            close.on('click',function () {
                obj.remove();
                return false;
            });
        }
    },
    widget:{//小组件
        init:function (obj) {
            this.share(obj);
            this.praise(obj);
            this.doiCode();
        },
        share: function (obj) { //分享
            $(obj).each(function () {
                var _share = $(this).find('a.share');
                var _shareBox = $(this).find('.share_box');
                var _el = _shareBox.find('.icons a');
                var _img = _shareBox.find('.qrcode img');
                //弹出框
                _share.on('click', function () {
                    _img.eq(1).show();
                    _shareBox.stop(true).fadeToggle();
                    return false;
                });

               /* //点击分享图标
                _el.on('click', function () {
                    $(this).addClass('active').siblings('a').removeClass('active');
                    _img.eq($(this).index()).show().siblings('img').hide();
                });

                _shareBox.on('click', function (e) {
                    e.preventDefault();
                    e.stopPropagation();
                });*/

                $(function(){
                    //分享至sina微博
                    var el=$('.card_main .floor a.share');
                    el.on('click',function(){
                        var img=$("#detailContent").find('img').attr('src').replace("../..","http://resource.efeiyi.com");
                        var title =$("#title").text()+"【非遗国际】";
                        var uri=location.href;
                        $('.share_box span').html('').append(shareSina(img,title,uri));
                    });




                });



                function shareSina(img,title,uri){
                    var str="<a class=\"sina\" href=\"javascript:void((function(s,d,e,r,l,p,t,z,c){var%20f='http://v.t.sina.com.cn/share/share.php?appkey=3348629102',u=z||d.location,p=['&url=',e(u),'&title=',e(t||d.title),'&source=',e(r),'&sourceUrl=',e(l),'&content=',c||'gb2312','&pic=',e(p||'')].join('');function%20a(){if(!window.open([f,p].join(''),'mb',['toolbar=0,status=0,resizable=1,width=440,height=430,left=',(s.width-440)/2,',top=',(s.height-430)/2].join('')))u.href=[f,p].join('');};if(/Firefox/.test(navigator.userAgent))setTimeout(a,0);else%20a();})(screen,document,encodeURIComponent,'','','"+img+"','"+title+"','"+uri+"','页面编码gb2312|utf-8默认gb2312'));\"></a>";
                    return str;
                }

                $(document).on("click", function () {
                    _shareBox.fadeOut();
                });
            });
        },
        praise: function (obj) { //点赞功能
            var praise = $(obj).find('.praise');
            praise.on('click', function () {
                var _this = $(this);

                //创建动画数字
                if (_this.hasClass('active')) { //取消点赞
                    _this.removeClass('active');
                    _this.append('<div class="add"><b>-1</b></div>');
                    animateNum('.add');
                } else { //点赞
                    _this.addClass('active');
                    _this.append('<div class="add"><b>+1</b></div>');
                    animateNum('.add');

                }

                function animateNum(obj) {
                    $(obj).css({
                        'position': 'absolute',
                        'z-index': '1',
                        'color': '#C30',
                        'left': '5px',
                        'top': '-15px',
                    }).animate({
                        left: 15,
                        top: -30
                    }, 'slow', function () {
                        $(this).fadeIn('fast').remove();
                    });
                }

            });

        },
        doiCode: function () { //doi鼠标滑过显示二维码
            // $('.doi_code').hover(function() {
            //     $(this).find('.drop').stop(true).fadeToggle(true);
            // });
        }
    },
    judgeDom:function () {
        //当doi编码不存在时隐藏div
        var doi_code = $("#doi_code").text().trim(" ");
        if(doi_code == null || doi_code == ""){
            $(".doi_code").hide();
        }

        //非遗在中国如果没有内容  就隐藏这个div
        if($("#subcon").find("span").length==0){
            $("#mas").css("display","none");
        }

    },
    code_arr:function () {//替换字典数据
        var code_arr = $('.dic');
        for(var i = 0; i < code_arr.length; i ++) {
            var _code = $(code_arr[i]).text();
            var _type = $(code_arr[i]).attr('dic-type');
            if(_type<100){
                $(code_arr[i]).text(_code);
            }
            var _lang = $(code_arr[i]).attr('lang');
            var _value = getTextByTypeAndCode(_type, _code, _lang);
            $(code_arr[i]).text(_value);
        }
    },
    catgary:function () {//查询分类
        var _catId = $("#category").attr("category-id");
        var text = getCategoryTextById(_catId);
        $("#category").text(text);
    },
    topPic:function () {//题图如果没有就动态创建默认图片  有就不创建
        var _src = $("#detailTopic").attr('src');
        if(_src=="" || _src ==null || typeof _src == 'undefined'){
            $('#detailContent').append('<img src="http://resource.efeiyi.com/image/uploads/head.png" alt="" id="back_img" style="width:2800px;height:600px; margin-left: -1400px;">')
            $('#detailContent').find('.mask_left').hide();
            $('#detailContent').find('.mask_right').hide();
        }
    },
    computeBaseInfo:function () {//基础信息部分
        var parent = $('#info');
        var li = parent.find('li');
        parent.find('li:even').css('float', 'left');
        parent.find('li:odd').css('float', 'right');
        li.each(function () {
            var key = $(this).find('span.key');
            var value = $(this).find('span.value');
            key.css('width', key.width() + 'px');
            value.css({
                'width': (520-key.width()) + 'px',
                'padding-left': key.width() + 'px'
            });
        });
    }
};



//静态页面搜素
var searchPage={
    init: function() {
        $('.header_detail .content .info li.login').addClass('line');
        this.initData();
        this.filterBar();
        this.search();
    },
    filterBar: function() {
        var obj = $('.filter_bar');
        var linkTab = obj.find('a');
        var iconTab = obj.find('.icon_tab');
        var proColumn = $('.pro_column3'); //搜索列表

        //筛选
        linkTab.on('click', function() {
            $(this).addClass('active').siblings('a').removeClass('active');

            //刷新搜索结果页

            if ($(this).index() == 0) {
                $("#type").val("");
            }
            if ($(this).index() == 1) {
                $("#type").val("0");
            }
            if ($(this).index() == 2) {
                $("#type").val("1");
            }
            if ($(this).index() == 3) {
                $("#type").val("2");
            }

            searchData_();

            return false;
        });

        //切换图标
        iconTab.on('click', function() {
            if ($(this).hasClass('active')) { //九宫格
                $(this).removeClass('active');
                proColumn.removeClass('active');
            } else { //横排
                $(this).addClass('active');
                proColumn.addClass('active');
            }
        });
    },
    search: function() {
        var filter = $('.filter_search'); //下拉搜索
        var filterFixed = $('.filter_search_fixed');
        var ipt = filter.find('.ipt');
        var iptVal = ipt.val();
        var filterAll = filter.find('.attr span'); //筛选项
        var filterItem = filter.find('.item'); //筛选下来框
        var suggest = filter.find('.suggest');
        var body = $('body');
        //获取焦点
        ipt.focus(function() {
            $(this).val('');
            body.append('<div class="overbg" style="z-index:1;"></div>');
        });

        //失去焦点如果为空则显示原始值
        ipt.blur(function() {
            var _val = $(this).val();
            if (_val == '') {
                $(this).val(iptVal);
            }
            $('.overbg').remove();
        });

        //2.点击筛选
        filterAll.on('click', function() {
            var _this = $(this);
            var _index = _this.index();
            filterItem.eq(_index)
                .css('left', parseInt(_this.position().left) + 'px')
                .show()
                .siblings('.item')
                .hide();
        });

        filterItem.each(function() {
            var _this = $(this);
            var level = $(this).find('.level');
            var level2 = $(this).find('.level2');
            var _li = level.find('li'); //分类

            _li.hover(function() {
                $(this).addClass('active').siblings('li').removeClass('active');

                $("#catecontent").empty();
                $("#citycontent").empty();

                if (typeof(category_all[$(this).index()].children) != "undefined") {
                    $.each(category_all[$(this).index()].children, function(index, content) {
                        $("#catecontent").append("<li data-id=\"" + content.gbCategory + "\" >" + content.name + "</li>");
                    });

                    //点击二级分类
                    $("#catecontent").find('li').on('click', function() {
                        filterAll.eq(0).text($(this).html());
                        _this.hide();
                        $("#gb_category_code").val($(this).attr("data-id"));
                        //searchData_();
                    });
                }

                if (typeof(dic_arr_city) != "undefined") {
                    $.each(dic_arr_city, function(index, content) {
                        $("#citycontent").append("<li data-id=\"" + content.code + "\"  >" + content.name + "</li>");
                    });

                    //国家级
                    $("#country").find('li').on('click', function() {
                        filterAll.eq(1).text($(this).html());
                        _this.hide();
                        $("#area_code").val("");
                        // searchData_();
                    });

                    //一级城市
                    $("#citycontent").find('li').on('click', function() {
                        filterAll.eq(1).text($(this).html());
                        _this.hide();
                        $("#area_code").val($(this).attr("data-id"));
                        //searchData_();
                    });
                }
                level2.show();
            });
        });


        //点击一级类别

        //3.阻止点击自身关闭
        filter.on('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
        });

        //4.点击自身之外的地方关闭下拉框
        $(document).on("click", function() {
            filterItem.hide();
            filterFixed.slideUp('fast');
        });
        //自动提示
        body.find('.overbg').on('click', function() {
            filterItem.hide();
            filterFixed.slideUp('fast');
            suggest.hide();
            $(this).remove();
            body.css('overflow', '');
        });

    },
    initData:function () {//初始化分类数据
        var mainCategory = $('#mainCategory');

        //初始化分类数据
        $.each(category_all, function(index, content) {
            mainCategory.append("<li data-id=\"" + content.gbCategory + "\" >" + content.name + "</li>");
        });

        mainCategory.find('li').on('click', function() {
            $("#attr_text").text($(this).html());
            $("#gb_category_code").val($(this).attr("data-id"));
            $("#item_1").hide();

            //searchData_();
        });
    },
    submit:function () {
        $(".form").ajaxSubmit();
    }
};

$(function () {
    searchPage.init();
})