package controllers;

import models.Poll;
import models.Polls;
import models.User;
import models.Users;
import org.codehaus.jackson.node.ArrayNode;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.JsonNode;

import play.libs.Json;

import scala.collection.JavaConversions;

import play.Logger;

public class JavaController extends Controller {

    public static Result index() {
        String screenName = session("user");
        scala.Option<User> optionalUser;

        if ( screenName != null ) {
            optionalUser = Users.byScreenName(screenName);
        } else {
            optionalUser = scala.Option.apply(null);
        }

        return ok(index.render(optionalUser, Polls.all(), flash()));
    }

    public static Result jsonPolls() {
        java.util.List<Poll> polls = JavaConversions.seqAsJavaList(Polls.all());

        ObjectNode node;
        ArrayNode root = Json.newObject().putArray("polls");

        for(Poll p: polls) {
            node = Json.newObject();
            node.put("url", routes.Application.answerForm(p.id()).absoluteURL(request()));
            node.put("id", p.id().toString());
            node.put("owner", p.owner());

            node.put("question", p.question());

            node.put("answer1", p.answer1());
            node.put("counterFor1", p.counterForOption(1));
            node.put("answer2", p.answer2());
            node.put("counterFor2", p.counterForOption(2));
            node.put("answer3", p.answer3());
            node.put("counterFor3", p.counterForOption(3));

            node.put("counter", p.counter());

            root.add(node);
        }
        return ok(root);
    }

}