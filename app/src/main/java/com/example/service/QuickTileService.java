package com.example.service;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QuickTileService extends TileService {

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        // 设置初始状态为关闭
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        tile.setLabel("点击切换");
        tile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();

        Tile tile = getQsTile();
        if (tile.getState() == Tile.STATE_INACTIVE) {
            // 状态从关闭切换到打开
            tile.setState(Tile.STATE_ACTIVE);
            Toast.makeText(this, "快捷按键已打开", Toast.LENGTH_SHORT).show();
        } else {
            // 状态从打开切换到关闭
            tile.setState(Tile.STATE_INACTIVE);
            Toast.makeText(this, "快捷按键已关闭", Toast.LENGTH_SHORT).show();
        }

        // 更新 Tile 状态
        tile.updateTile();
    }
}

