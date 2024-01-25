-module(server_handler).
-export([init/2, websocket_handle/2, websocket_info/2]).

% State = {PidOtherPlayer1, PidOtherPlayer2, WordToGuess}
init (Req, State) ->
	{cowboy_websocket, Req, {undefined, undefined, undefined, undefined, ""}}.


websocket_handle(Frame = {text, Json}, State = {Username, Role, Player1, Player2, WordToGuess}) ->
	io:format("[chatroom_websocket] websocket_handle => Frame: ~p, State: ~p~n", [Json, State]),
	DecodedMessage = jsx:decode(Json),
	Action = maps:get(<<"action">>, DecodedMessage),
	{Response, UpdatedState} =
		if
			Action == <<"login">> ->
				S = player_handler:login(DecodedMessage, State),
				{Frame, S};
			Action == <<"start">> ->
				player_handler:start(DecodedMessage, State);
			Action == <<"word">> ->
				S = player_handler:word(DecodedMessage, State),
				{Frame, S};
			Action == <<"wait">> ->
				io:format("~p starta l'attesa~n", [Username]),
				player_handler:wait_for_messages(State);
			Action == <<"guess">> ->
				player_handler:guess(DecodedMessage, State)
		end,
	io:format("~p Response ~p~n", [Username,Response]),
	{reply, [Response], UpdatedState}.


websocket_info({wordFromOthers, WordMsg}, State) ->
	io:format("=> Word from others ~p~p~n", [WordMsg, State]),
	JsonMessage = jsx:encode([{<<"type">>, word}, {<<"msg">>, WordMsg}]),
	{[{text, JsonMessage}], State};
websocket_info({guessFromOthers, {GuessMsg, NewWordToGuess}}, State = {Username, Role, Player1, Player2, WordToGuess}) ->
	io:format("=> Guess from others ~p~p~n", [GuessMsg, State]),
	M = binary_to_list(GuessMsg),
	io:format("info qui~n"),
	if M == WordToGuess -> Res = ok;
		true -> Res = no
	end,
	io:format("info info qui~n"),
	JsonMessage = jsx:encode([{<<"type">>, guessWord}, {<<"msg">>, GuessMsg},{<<"res">>, Res},{<<"newWord">>, NewWordToGuess}]),
	io:format("info info info qui~n"),
	{[{text, JsonMessage}], {Username, Role, Player1, Player2, NewWordToGuess}};
websocket_info(Info, State) ->
	io:format("chatroom_websocket:websocket_info(Info, State) => Received info ~p~n", [Info]),
	{ok, State}.
