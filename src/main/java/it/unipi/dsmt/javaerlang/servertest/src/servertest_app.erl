%%%-------------------------------------------------------------------
%% @doc servertest public API
%% @end
%%%-------------------------------------------------------------------

-module(servertest_app).

-behaviour(application).

-export([start/2, stop/1]).

start(_StartType, _StartArgs) ->
	Dispatch = cowboy_router:compile([
		{'_', [{"/erlServer", server_handler, []}]}
	]),
	{ok, Pid} = cowboy:start_clear(http_listener,
		[{port, 8090}],
		#{env => #{dispatch => Dispatch}}
	),
    servertest_sup:start_link().

stop(_State) ->
    ok.

%% internal functions
