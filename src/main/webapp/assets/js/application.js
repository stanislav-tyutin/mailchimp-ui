class List {
    constructor(data) {
        this.id = data.id;
        this.name = data.name;

        this.content_created = false;
    }

    addToMenu() {
        let self = this;
        $("#mailing_lists").append("<li id='ml" + this.id + "'></li>");
        $("#ml" + this.id).html("<a href='#'>" + this.name + "</a>").click(function () {
            self.showContent();
            return false;
        });
    }

    showContent() {
        let self = this;
        let content_div = $("#content");
        content_div.find(".list_content").hide();

        if (this.content_created) {
            $("#mlc" + this.id).show();
        }
        else {
            this.content_created = true;
            content_div.append("<div id='mlc" + this.id +
                "' class='list_content'><div class='row'><div class='data columns large-6'></div><div class='actions columns large-6'></div></div></div>");
            let data_content = $("#mlc" + this.id).find('.data');
            let actions_content = $("#mlc" + this.id).find('.actions');
            data_content.html("<h4>Список рассылки " + this.name + "</h4><ul class='list_members'><ul>");
            actions_content.html("<button class='button btn_add_member'>Добавить email</button><br/><button class='button btn_send'>Выполнить рассылку</button>")
            actions_content.find('.btn_add_member').click(function () {
                self.addEmail();
            });
            actions_content.find('.btn_send').click(function () {
                self.runCampaign();
            });
            this.loadAndShowMembers();
        }
    }

    addListMember(name) {
        let self = this;
        let data_content = $("#mlc" + this.id).find('.data');
        let ul = data_content.find(".list_members");
        let li = ul.append("<li></li>").find('li').last();
        li.append(name);
        li.append("<br/><button class='small alert button'>Удалить</button>").find("button").click(function () {
            $.post("/method/deleteEmail", {list_id: self.id, email: name}).success(function () {
                let data_content = $("#mlc" + self.id).find('.data');
                let ul = data_content.find(".list_members");
                ul.html("");
                self.loadAndShowMembers();
            });
        });
    }

    loadAndShowMembers() {
        let self = this;
        let data_content = $("#mlc" + this.id).find('.data');
        let ul = data_content.find(".list_members");
        $.get("/method/getListMembers?list_id=" + this.id).success(function (data) {
            data.forEach(function (name) {
                self.addListMember(name);
            });
        });
    }

    addEmail() {
        let self = this;
        let email = prompt("Введите email");
        $.post("/method/addEmail", {list_id: this.id, email: email}).success(function () {
            self.addListMember(email);
        });
    }

    runCampaign() {
        let text = prompt("Введите текст сообщения");
        $.post("/method/sendMessages", {list_id: this.id, text: text}).success(function () {
            alert("Готово");
        });
    }

}

function loadLists() {
    $.get("/method/getLists").success(function (data) {
        data.forEach(function (el) {
            let obj = new List(el);
            obj.addToMenu();
        });
    });
}


$(document).ready(function () {

    loadLists();

    $("#btn_create_list").click(function () {
        let name = prompt("Введите имя нового списка");
        $.post("/method/createList", {list_name: name}).success(function () {
            $("#mailing_lists").html("");
            let content_div = $("#content");
            content_div.find(".list_content").remove();
            loadLists();
        });
    });

});

	