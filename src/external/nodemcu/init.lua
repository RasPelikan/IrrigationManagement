_G.irrigators = {}

function parseRequest(request)
    local first = nil
    for line in string.gmatch(request, "[^\n]+") do
        if (first == nil) then
            first = line
        end
    end

    if (first == nil) then
        return
    end

    local method = nil
    local path = nil
    for part in string.gmatch(first, "[^ ]+") do
        if (method == nil) then
            method = part
        else
            if (path == nil) then
                path = part
            end
        end
    end

    local func = nil
    local arg = nil
    for part in string.gmatch(path, "[^ /]+") do
        if (func == nil) then
            func = part
        else
            if (arg == nil) then
                arg = part
            end
        end
    end

    return func, arg
end

function irrigatorGpio(which)
    if (which == 0) then
        return 1
    elseif (which == 1) then
        return 2
    elseif (which == 2) then
        return 3
    elseif (which == 3) then
        return 5
    end
    return -1
end

function stopIrrigator(timer)
    local id = timer
    local start = _G.irrigators[id]
    local time = tmr.now() - start
    if (time > 1000) then
        print("stop irrigator " .. id)
        local gpioId = irrigatorGpio(id)
        gpio.write(gpioId, gpio.HIGH)
    end
end

function startIrrigator(which)
    if (which == nil) then
        return "400 Bad Request", ""
    end

    local id = tonumber(which)
    _G.irrigators[id] = tmr.now()
    local gpioId = irrigatorGpio(id)
    gpio.write(gpioId, gpio.LOW)
    tmr.alarm(id, 1500, tmr.ALARM_SINGLE, stopIrrigator)
    
    return "200 OK", which
end

function rainSensor(arg)
    return "200 OK", "RAIN"
end

function reboot(arg)
    print("Will reboot in 5 seconds")
    tmr.alarm(6, 5000, tmr.ALARM_SINGLE, function()
        print("Will reboot now")
        node.restart()
    end)
    return "200 OK", "REBOOT"
end

function receive(conn, payload)
    local func, arg = parseRequest(payload)

    local response = nil
    local code = nil
    if (func == "irrigator") then
        code, response = startIrrigator(arg)
    elseif (func == "rainsensor") then
        code, response = rainSensor(arg)
    elseif (func == "reboot") then
        code, response = reboot(arg)
    else
        code = "400 Bad Request"
        response = ""
    end

    local len = string.len(response)

    conn:on("sent", function(sck) sck:close() end)
    conn:send("HTTP/1.1 " .. code .. "\r\nConnection: close\r\nContent-Type: text/plain\r\nContent-Length:" .. len .. "\r\n\r\n" .. response)
end

function connection(conn) 
    conn:on("receive", receive)
end

function startServer()
    local ip = wifi.sta.getip()
    print("Got ip: " .. ip)

    if (srv) then
      srv:close()
    end
    print("Start server on port 8080")
    srv=net.createServer(net.TCP, 30) 
    srv:listen(8080, ip, connection)
end

wifi.eventmon.register(wifi.eventmon.STA_GOT_IP, startServer)
wifi.eventmon.register(wifi.eventmon.STA_DHCP_TIMEOUT, startServer)
wifi.setmode(wifi.STATION)
wifi.setphymode(wifi.PHYMODE_G)
local netId = "IRIG-" .. node.chipid()
print("Setup WiFi as " .. netId)
wifi.sta.sethostname(netId)

local station_cfg = {}
station_cfg.ssid = "NETGEAR"
station_cfg.pwd = "123456789012345"
wifi.sta.config(station_cfg)

-- blue light
gpio.mode(0, gpio.OUTPUT)
gpio.write(0, gpio.HIGH)
gpio.mode(1, gpio.OUTPUT)
gpio.write(1, gpio.HIGH)
gpio.mode(2, gpio.OUTPUT)
gpio.write(2, gpio.HIGH)
gpio.mode(3, gpio.OUTPUT)
gpio.write(3, gpio.HIGH)
gpio.mode(5, gpio.OUTPUT)
gpio.write(5, gpio.HIGH)
