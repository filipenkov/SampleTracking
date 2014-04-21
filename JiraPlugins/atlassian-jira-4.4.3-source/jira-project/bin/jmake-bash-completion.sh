## bash completion for jmake
##
## Linux:    copy into /etc/bash-completion.d
## MacPorts: copy into /opt/local/etc/bash-completion.d
##
_jmake()
{
    local cur prev opts
    COMPREPLY=()
    cur="${COMP_WORDS[COMP_CWORD]}"
    prev="${COMP_WORDS[COMP_CWORD-1]}"
    opts="`./jmake help | tail +3 | cut -d ' ' -f 2 | sort | xargs echo`"

    if [[ ${cur} == * ]] ; then
        COMPREPLY=( $(compgen -W "${opts}" ${cur}) )
        return 0
    fi
}
complete -F _jmake jmake
