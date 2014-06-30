//
//  LruCacheHelper.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/05/29.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceApp.Common.Utilities
{
    public class LruCacheHelper<K, V>
    {
        private Dictionary<K, V> _hashMap;
        private LinkedList<K> _queue = new LinkedList<K>();
        private object _cacheLock = new object();
        private int _max;

        public LruCacheHelper(int capacity)
        {
            _hashMap = new Dictionary<K, V>(capacity);
            _max = capacity;
        }

        /// <summary>
        /// Inserts an item into the LRU cache.
        /// Removes duplicate (old) values if same key exists.
        /// </summary>
        /// <param name="key">key</param>
        /// <param name="value">value</param>
        public void Add(K key, V value)
        {
            lock (_cacheLock)
            {
                RemoveDuplicateKey(key);
                CheckCacheLimit();
                _queue.AddLast(key);
                _hashMap[key] = value;
            }
        }

        /// <summary>
        /// Deletes an item from the LRU cache
        /// </summary>
        /// <param name="key">key</param>
        public void Delete(K key)
        {
            lock (_cacheLock)
            {
                _hashMap.Remove(key);
                _queue.Remove(key);
            }
        }

        /// <summary>
        /// Clears all items of the LRU cache
        /// </summary>
        public void Clear()
        {
            lock (_cacheLock)
            {
                _hashMap.Clear();
                _queue.Clear();
            }
        }

        /// <summary>
        /// Gets the value associate to the key
        /// </summary>
        /// <param name="key">key</param>
        /// <returns>value if found, null otherwise</returns>
        public V GetValue(K key)
        {
            lock (_cacheLock)
            {
                V ret;
                if (_hashMap.TryGetValue(key, out ret))
                {
                    _queue.Remove(key);
                    _queue.AddLast(key);
                }

                return ret;
            }
        }

        /// <summary>
        /// Checks if a key already exists
        /// </summary>
        /// <param name="key">key</param>
        /// <returns>true if found, false otherwise</returns>
        public bool ContainsKey(K key)
        {
            bool ret = false;

            lock (_cacheLock)
            {
                ret = _hashMap.ContainsKey(key);
            }

            return ret;
        }

        /// <summary>
        /// Removes existing entry based on its key
        /// </summary>
        /// <param name="key">key</param>
        private void RemoveDuplicateKey(K key)
        {
            lock (_cacheLock)
            {
                if (ContainsKey(key))
                {
                    _queue.Remove(key);
                    _hashMap.Remove(key);
                }
            }
        }

        /// <summary>
        /// Checks if the cache is full and removes old entry if needed
        /// </summary>
        private void CheckCacheLimit()
        {
            lock (_cacheLock)
            {
                if (_hashMap.Count == _max)
                {
                    var node = _queue.First;
                    _hashMap.Remove(node.Value);
                    _queue.RemoveFirst();
                }
            }
        }

    }
}
